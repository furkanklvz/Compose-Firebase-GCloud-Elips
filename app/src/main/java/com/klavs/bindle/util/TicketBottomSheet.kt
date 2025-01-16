package com.klavs.bindle.util

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.rounded.AddLocation
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalActivity
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.klavs.bindle.R
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.resource.RewardedAdResource
import com.klavs.bindle.uix.viewmodel.TicketDialogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    uid: String,
    tickets: Long
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewModel = hiltViewModel<TicketDialogViewModel>()
    val productsResource by viewModel.productsResource.collectAsState()
    var isRewardButtonClicked by remember { mutableStateOf(false) }
    val rewardedAd by viewModel.rewardedAd.collectAsState()
    val rewardedAdContentStateResource by viewModel.rewardedAdContentStateResource.collectAsState()
    val rewardResource by viewModel.rewardResource.collectAsState()
    var isLoading by remember { mutableStateOf(false) }


    val productsResourcePair = when (productsResource) {
        is Resource.Error -> Resource.Error(messageResource = productsResource.messageResource!!)
        is Resource.Idle -> Resource.Idle()
        is Resource.Loading -> Resource.Loading()
        is Resource.Success -> {
            Resource.Success(
                data = productsResource.data!!
                    .sortedBy { it.oneTimePurchaseOfferDetails?.priceAmountMicros }
                    .map { it.oneTimePurchaseOfferDetails?.formattedPrice to it.name }
            )
        }
    }

    LaunchedEffect(rewardedAd) {
        if (isRewardButtonClicked) {
            when (rewardedAd) {
                is Resource.Error -> {
                    viewModel.loadRewardedAd(context)
                }

                is Resource.Idle -> {
                    viewModel.loadRewardedAd(context)
                }

                is Resource.Loading -> {
                    isLoading = true
                }

                is Resource.Success -> {
                    if (activity != null) {
                        viewModel.showRewardedAd(
                            rewardedAd = rewardedAd.data!!,
                            activity = activity
                        )
                    }
                    isRewardButtonClicked = false
                }
            }
        }
    }



    LaunchedEffect(true) {
        viewModel.startConnection()
        Log.e("purchase", "start connection çalıştı")

        if (rewardedAd is Resource.Idle || rewardedAd is Resource.Error) {
            viewModel.loadRewardedAd(
                context = context
            )
        }
    }

    TicketBottomSheet(
        onDismiss = {
            viewModel.reset()
            onDismiss()
        },
        sheetState = sheetState,
        rewardResource = rewardResource,
        resetRewardResource = { viewModel.resetRewardResource() },
        rewardedAdContentStateResource = rewardedAdContentStateResource,
        reward = {
            viewModel.reward(
                uid = uid,
                currentTickets = tickets,
                rewardAmount = rewardedAdContentStateResource!!.rewardAmount!!,
                rewardType = rewardedAdContentStateResource!!.rewardType!!
            )
        },
        resetRewardedAdContentStateResource = { viewModel.resetRewardedAdContentStateResource() },
        isLoading = isLoading,
        rewardButtonClicked = isRewardButtonClicked,
        productsResource = productsResourcePair,
        tickets = tickets,
        onAdsClick = {
            when (rewardedAd) {
                is Resource.Success -> {
                    if (activity != null) {
                        viewModel.showRewardedAd(
                            rewardedAd = rewardedAd.data!!,
                            activity = activity
                        )
                    }
                }

                is Resource.Error -> {
                    isRewardButtonClicked = true
                }

                is Resource.Idle -> {
                    viewModel.loadRewardedAd(context)
                    isRewardButtonClicked = true
                }

                is Resource.Loading -> {
                    isRewardButtonClicked = true
                }
            }
        },
        purchase = { productName ->
            if (activity != null && productsResource.data != null) {
                viewModel.purchase(
                    productDetails = productsResource.data!!.find { it.name == productName }!!,
                    activity = activity
                )
            }
        }
    )

}


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TicketBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    rewardResource: Resource<Pair<Int, Int>>,
    productsResource: Resource<List<Pair<String?, String>>>,
    resetRewardResource: () -> Unit,
    resetRewardedAdContentStateResource: () -> Unit,
    rewardedAdContentStateResource: RewardedAdResource?,
    isLoading: Boolean,
    tickets: Long,
    reward: () -> Unit,
    purchase: (String) -> Unit,
    onAdsClick: () -> Unit,
    rewardButtonClicked: Boolean,
) {
    ModalBottomSheet(
        modifier = Modifier.fillMaxSize(),
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color.Transparent
            )
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            when (rewardResource) {
                is Resource.Error -> {
                    AlertDialog(
                        onDismissRequest = resetRewardResource,
                        confirmButton = {
                            Button(
                                onClick = resetRewardResource
                            ) {
                                Text(stringResource(R.string.okay))
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "on error"
                            )
                        },
                        title = {
                            Text(stringResource(R.string.reward_could_not_be_received_dialog_title))
                        },
                        text = {
                            Text(stringResource(R.string.reward_could_not_be_received_dialog_text))
                        })
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    Dialog(
                        onDismissRequest = {},
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        )
                    ) {
                        CircularWavyProgressIndicator()
                    }
                }

                is Resource.Success -> {
                    AlertDialog(
                        onDismissRequest = resetRewardResource,
                        confirmButton = {
                            Button(
                                onClick = resetRewardResource
                            ) {
                                Text(stringResource(R.string.great))
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.LocalActivity,
                                contentDescription = "success"
                            )
                        },
                        title = {
                            Text(stringResource(R.string.congrats))
                        },
                        text = {
                            Text(
                                stringResource(
                                    R.string.you_won_x_y,
                                    rewardResource.data!!.first,
                                    stringResource(rewardResource.data.second)
                                )
                            )
                        }
                    )
                }
            }
            when (rewardedAdContentStateResource) {
                is RewardedAdResource.OnUserEarnedReward -> {
                    reward()
                }

                is RewardedAdResource.onAdClicked -> {}
                is RewardedAdResource.onAdDismissedFullScreenContent -> {}

                is RewardedAdResource.onAdFailedToShowFullScreenContent -> {
                    AlertDialog(
                        onDismissRequest = resetRewardedAdContentStateResource,
                        confirmButton = {
                            Button(
                                onClick = resetRewardedAdContentStateResource
                            ) {
                                Text(stringResource(R.string.okay))
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "on failed"
                            )
                        },
                        title = {
                            Text(stringResource(R.string.ad_could_not_be_loaded))
                        },
                        text = {
                            Text(stringResource(R.string.failed_to_show_ad_dialog_text))
                        })
                }

                is RewardedAdResource.onAdImpression -> {}
                is RewardedAdResource.onAdShowedFullScreenContent -> {}
                null -> {}
            }

            if (rewardButtonClicked) {
                if (isLoading) {
                    Dialog(
                        onDismissRequest = {},
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        )
                    ) {
                        CircularWavyProgressIndicator()
                    }
                }
            }
            when (productsResource) {
                is Resource.Error -> {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            productsResource.messageResource?.let { stringResource(it) } ?: "",
                            modifier = Modifier
                                .padding(6.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(2f)
                    ) {
                        CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is Resource.Success -> {
                    if (productsResource.data != null) {
                        Log.e("ticketdialog", "success")
                        Content(
                            products = productsResource.data,
                            tickets = tickets,
                            onAdsClick = onAdsClick,
                            onProductClick = { productName ->
                                purchase(productName)
                            },
                            onDismissDialog = onDismiss
                        )

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Content(
    products: List<Pair<String?, String>>,
    tickets: Long,
    onAdsClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onDismissDialog: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Box(Modifier.fillMaxWidth()) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(IconButtonDefaults.mediumContainerSize()),
                onClick = onDismissDialog
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "dismiss"
                )
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.LocalActivity,
                    contentDescription = "tickets",
                    modifier = Modifier
                        .padding(5.dp)
                        .size(IconButtonDefaults.xLargeIconSize),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.you_have_x_ticket_section_one))
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = ButtonDefaults.textButtonColors().contentColor
                            ),
                        ) {
                            append("$tickets")
                        }
                        append(stringResource(R.string.you_have_x_ticket_section_two))
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(5.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

        }


        Text(
            stringResource(R.string.you_want_more),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleSmall
        )
        Button(
            shape = RoundedCornerShape(10.dp),
            onClick = onAdsClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Outlined.SlowMotionVideo, contentDescription = "watch an ads")
            Text(
                stringResource(R.string.watch_an_ads), modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            stringResource(R.string.or), modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleSmall
        )

        if (products.size == 3) {
            Column(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                products.forEachIndexed { index, productDetails ->
                    val price =
                        productDetails.first ?: "N/A"


                    ListItem(
                        tonalElevation = 5.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxWidth(0.9f)
                            .clickable { onProductClick(productDetails.second) },
                        headlineContent = {
                            Text(
                                productDetails.second,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.LocalActivity,
                                contentDescription = "tickets",
                                tint = when (index) {
                                    0 -> LocalContentColor.current
                                    1 -> MaterialTheme.colorScheme.secondary
                                    2 -> MaterialTheme.colorScheme.tertiary
                                    else -> LocalContentColor.current
                                }
                            )
                        },
                        trailingContent = {
                            Text(
                                price,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

            }
        }
        Column(
            Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {
            Text(
                "${stringResource(R.string.costs)}:",
                modifier = Modifier
                    .align(Alignment.Start),
                style = MaterialTheme.typography.titleSmall
            )
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.AddLocation,
                        contentDescription = "creat"
                    )
                },
                headlineContent = {
                    Text(
                        text = stringResource(R.string.creation_of_event_or_community),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("3", style = MaterialTheme.typography.titleLarge)
                        Icon(
                            imageVector = Icons.Rounded.LocalActivity,
                            contentDescription = "3 tickets",
                            modifier = Modifier
                                .padding(start = 3.dp)
                                .size(IconButtonDefaults.xSmallIconSize)
                        )
                    }
                }
            )
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.participation_of_event_or_community),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = "participating"
                    )
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("2", style = MaterialTheme.typography.titleLarge)
                        Icon(
                            imageVector = Icons.Rounded.LocalActivity,
                            contentDescription = "2 tickets",
                            modifier = Modifier
                                .padding(start = 3.dp)
                                .size(IconButtonDefaults.xSmallIconSize)
                        )
                    }
                }
            )
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.changing_information_of_event),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "creat"
                    )
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("1", style = MaterialTheme.typography.titleLarge)
                        Icon(
                            imageVector = Icons.Rounded.LocalActivity,
                            contentDescription = "1 tickets",
                            modifier = Modifier
                                .padding(start = 3.dp)
                                .size(IconButtonDefaults.xSmallIconSize)
                        )
                    }
                }
            )
        }


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showSystemUi = true, showBackground = true,
)
@Composable
private fun TicketDialogPreview() {
    Box(Modifier.fillMaxSize()) {
        TicketBottomSheet(
            onDismiss = {},
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            rewardResource = Resource.Idle(/*data = 1 to R.string.ticket*/),
            productsResource = Resource.Success(
                data = listOf(
                    "TRY 19" to "10 Tickets",
                    "TRY 29" to "20 Tickets",
                    "TRY 39" to "30 Tickets",
                )
            ),
            resetRewardResource = {},
            resetRewardedAdContentStateResource = {},
            rewardedAdContentStateResource = null,
            isLoading = false,
            tickets = 5,
            reward = {},
            purchase = {},
            onAdsClick = {},
            rewardButtonClicked = false
        )
    }

}