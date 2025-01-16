package com.klavs.bindle.uix.view.communities.communityPage

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.util.TimeFunctions
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.LightRed
import com.klavs.bindle.uix.view.event.getEventIconFromValue
import com.klavs.bindle.util.EventBottomSheet
import com.klavs.bindle.uix.viewmodel.communities.CommunityEventListViewModel
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.communities.CommunityPageViewModel
import com.klavs.bindle.util.TicketBottomSheet
import com.klavs.bindle.util.UnverifiedAccountAlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActiveEventsListPage(
    navController: NavHostController,
    communityId: String,
    currentUser: FirebaseUser,
    navHostViewModel: NavHostViewModel,
    eventListViewModel: CommunityEventListViewModel,
    communityPageViewModel: CommunityPageViewModel,
    communityName: String
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val upcomingEventsResource by communityPageViewModel.upcomingEventsResourceFlow.collectAsState()
    val myMemberDocResource by communityPageViewModel.myMemberDocResource.collectAsState()
    val userResourceFlow by navHostViewModel.userResourceFlow.collectAsState()
    val unlinkEventResource by eventListViewModel.unlinkEventResource.collectAsState()
    val pastEvents = remember { mutableStateListOf<Event>() }
    val pastEventsResource by eventListViewModel.pastEvents.collectAsState()
    LaunchedEffect(pastEventsResource) {
        if (pastEventsResource is Resource.Success && pastEventsResource.data != null) {
            pastEvents.addAll(pastEventsResource.data!!)
        }
    }

    LaunchedEffect(true) {
        Log.d("communityPage", "upcomingEventsResource.data: ${upcomingEventsResource.data}")
        Log.d("communityPage", "myMemberDocResource.data: ${myMemberDocResource.data}")
        if (myMemberDocResource.data?.communityId != communityId) {
            communityPageViewModel.listenToMyMemberDoc(
                communityId = communityId,
                myUid = currentUser.uid
            )
        }
    }
    LaunchedEffect(unlinkEventResource) {
        if (unlinkEventResource is Resource.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    unlinkEventResource.messageResource?.let { context.getString(it) } ?: ""
                )
            }
        }
    }
    CommunityEventListContent(
        onBackClick = { navController.popBackStack() },
        scope = scope,
        snackbarHostState = snackbarHostState,
        goToProfile = { navController.navigate("menu_profile") },
        communityName = communityName,
        userResourceFlow = userResourceFlow,
        currentUser = currentUser,
        goToCommunityPage = { navController.navigate("community_page/$it") },
        navHostViewModel = navHostViewModel,
        navigateToEventPage = { navController.navigate("event_page/$it") },
        navigateToLogIn = { navController.navigate("log_in") },
        pastEventsResource = pastEventsResource,
        upcomingEventsResoure = upcomingEventsResource,
        getPasEvents = {
            eventListViewModel.getPastEvents(
                communityId = communityId,
                pageSize = it
            )
        },
        resetLastPastEvent = { eventListViewModel.lastPastEvent = null },
        myRole = myMemberDocResource.data?.rolePriority ?: CommunityRoles.Member.rolePriority,
        unlinkEvent = {
            eventListViewModel.unlinkEvent(
                eventId = it,
                communityId = communityId
            )
        },
        thereIsLastPastEvent = eventListViewModel.lastPastEvent != null,
        pastEvents = pastEvents,
        tryAgainOnClick = {communityPageViewModel.getUpcomingEvents(communityId)}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityEventListContent(
    onBackClick: () -> Unit,
    goToCommunityPage: (String) -> Unit,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    communityName: String,
    unlinkEvent: (String) -> Unit,
    navigateToEventPage: (String) -> Unit,
    navigateToLogIn: () -> Unit,
    navHostViewModel: NavHostViewModel? = null,
    currentUser: FirebaseUser? = null,
    thereIsLastPastEvent: Boolean,
    userResourceFlow: Resource<User>,
    tryAgainOnClick :()->Unit,
    getPasEvents: (Int) -> Unit,
    resetLastPastEvent: () -> Unit,
    pastEvents: List<Event>,
    myRole: Int,
    pastEventsResource: Resource<List<Event>>,
    upcomingEventsResoure: Resource<List<Event>>,
    goToProfile: () -> Unit
) {
    val context = LocalContext.current
    var showTicketSheet by remember { mutableStateOf(false) }
    val ticketSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showUnverifiedAccountAlertDialog by remember { mutableStateOf(false) }
    var showEventBottomSheet by remember { mutableStateOf<Event?>(null) }
    val eventBottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.imePadding())
        },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                title = {
                    Text(
                        communityName, style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (showUnverifiedAccountAlertDialog) {
                UnverifiedAccountAlertDialog(
                    onDismiss = { showUnverifiedAccountAlertDialog = false }
                ) {
                    goToProfile()
                }
            }
            if (showTicketSheet) {
                if (userResourceFlow is Resource.Success) {
                    if (currentUser != null) {
                        TicketBottomSheet(
                            onDismiss = { scope.launch { ticketSheetState.hide() }.invokeOnCompletion {
                                if (!ticketSheetState.isVisible){
                                    showTicketSheet = false
                                }
                            } },
                            uid = currentUser.uid,
                            tickets = userResourceFlow.data!!.tickets,
                            sheetState = ticketSheetState
                        )
                    }
                }
            }

            if (showEventBottomSheet != null) {
                if (navHostViewModel != null) {
                    EventBottomSheet(
                        onDismiss = {
                            scope.launch { eventBottomSheetState.hide() }.invokeOnCompletion {
                                if (!eventBottomSheetState.isVisible){
                                    showEventBottomSheet = null
                                }
                            }
                        },
                        sheetState = eventBottomSheetState,
                        context = LocalContext.current,
                        onCommunityClick = { goToCommunityPage(it) },
                        currentUser = currentUser,
                        event = showEventBottomSheet!!,
                        navHostViewModel = navHostViewModel,
                        showTicketSheet = { showTicketSheet = true },
                        showUnverifiedAccountAlertDialog = {
                            showUnverifiedAccountAlertDialog = true
                        },
                        navigateToEventPage = {
                            navigateToEventPage(it)
                        },
                        onLogInClick = navigateToLogIn
                    )
                }
            }
            Column(Modifier.fillMaxSize()) {
                val tabTitles = listOf(
                    stringResource(R.string.upcoming_events),
                    stringResource(R.string.past_events)
                )
                var state by remember { mutableIntStateOf(0) }
                PrimaryTabRow(
                    divider = {},
                    selectedTabIndex = state
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val selected = state == index
                        Tab(
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            icon = {
                                if (index == 0) {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Upcoming,
                                            contentDescription = "upcoming"
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Upcoming,
                                            contentDescription = "upcoming"
                                        )
                                    }

                                } else {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Restore,
                                            contentDescription = "past"
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Restore,
                                            contentDescription = "past"
                                        )
                                    }

                                }
                            },
                            selected = selected,
                            onClick = {
                                state = index
                                if (state == 1 && (pastEventsResource is Resource.Idle || pastEventsResource is Resource.Error)) {
                                    resetLastPastEvent()
                                    getPasEvents(5)
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
                when (state) {
                    0 -> {
                        UpcomingEventList(
                            onEventClick = {
                                showEventBottomSheet = it
                            },
                            myRole = myRole,
                            onUnlinkClick = {

                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.unlink_event_snackbar_message),
                                        actionLabel = context.getString(R.string.unlink),
                                        duration = SnackbarDuration.Long,
                                        withDismissAction = true
                                    )
                                    when (result) {
                                        SnackbarResult.Dismissed -> {}
                                        SnackbarResult.ActionPerformed -> {
                                            if (myRole != CommunityRoles.Member.rolePriority) {
                                                unlinkEvent(it)
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                            } else {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                snackbarHostState.showSnackbar(
                                                    message = context.getString(R.string.unlink_event_snackbar_permission_message)
                                                )
                                            }

                                        }
                                    }
                                }

                            },
                            upcomingEventsResource = upcomingEventsResoure,
                            tryAgainOnClick = tryAgainOnClick
                        )
                    }

                    1 -> {
                        PastEventList(
                            onEventClick = {
                                showEventBottomSheet = it
                            },
                            pastEvents = pastEvents,
                            pastEventsResource = pastEventsResource,
                            onLoadMore = {
                                getPasEvents(5)
                            },
                            thereIsLastPastEvent = thereIsLastPastEvent,
                        )
                    }
                }
            }

        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UpcomingEventList(
    upcomingEventsResource: Resource<List<Event>>,
    myRole: Int,
    onUnlinkClick: (String) -> Unit,
    tryAgainOnClick: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    when (upcomingEventsResource) {
        is Resource.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        stringResource(upcomingEventsResource.messageResource!!),
                        style = MaterialTheme.typography.titleSmall
                    )
                    IconButton(
                        onClick = tryAgainOnClick
                    ) {
                        Icon(imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = "retry")
                    }
                }
            }
        }

        is Resource.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator()
            }
        }

        is Resource.Success -> {
            LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (upcomingEventsResource.data!!.isEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "empty"
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                stringResource(R.string.no_upcoming_events),
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(upcomingEventsResource.data) { event ->

                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier.clickable {
                                onEventClick(event)
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.width(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (event.privateInfo) {
                                        Text(
                                            "${stringResource(R.string._private)}\n${
                                                stringResource(
                                                    R.string.date
                                                )
                                            }",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        Text(
                                            TimeFunctions().convertTimestampToLocalizeDate(
                                                event.date,
                                                singleLine = false
                                            ),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                            },
                            headlineContent = { Text(event.title) },
                            trailingContent = {
                                if (myRole != CommunityRoles.Member.rolePriority) {
                                    var eventMenuExpanded by remember { mutableStateOf(false) }
                                    IconButton(
                                        onClick = { eventMenuExpanded = !eventMenuExpanded }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreVert,
                                            contentDescription = "more"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = eventMenuExpanded,
                                        onDismissRequest = { eventMenuExpanded = false }
                                    ) {

                                        DropdownMenuItem(
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Rounded.LinkOff,
                                                    contentDescription = "unlink",
                                                    tint = LightRed
                                                )
                                            },
                                            text = {
                                                Text(
                                                    stringResource(R.string.unlink_event_text),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = LightRed
                                                )
                                            },
                                            onClick = { onUnlinkClick(event.id) }
                                        )
                                    }
                                }
                            },
                            supportingContent = {
                                if (!event.privateInfo) {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = Icons.Rounded.AccessTime,
                                            contentDescription = "time",
                                            modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                                        )
                                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        Text(sdf.format(event.date.toDate()))
                                    }
                                }
                            }
                        )
                        if (event != upcomingEventsResource.data.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        is Resource.Idle -> {}
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PastEventList(
    onEventClick: (Event) -> Unit,
    pastEvents: List<Event>,
    thereIsLastPastEvent: Boolean,
    onLoadMore: () -> Unit,
    pastEventsResource: Resource<List<Event>>,
) {
    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    LaunchedEffect(pastEventsResource) {
        when (pastEventsResource) {
            is Resource.Error -> {
                isLoading = false
                isError = true
            }

            is Resource.Idle -> {
                isLoading = false
                isError = false
            }

            is Resource.Loading -> {
                isLoading = true
                isError = false
            }

            is Resource.Success -> {
                isLoading = false
                isError = false
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        items(pastEvents) { event ->
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.clickable {
                    onEventClick(event)
                },
                leadingContent = {
                    Icon(
                        imageVector = getEventIconFromValue(event.type),
                        contentDescription = "more",
                        modifier = Modifier.size(IconButtonDefaults.xLargeIconSize)
                    )
                },
                headlineContent = { Text(event.title) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "date"
                        )
                        Text(TimeFunctions().convertTimestampToLocalizeDate(event.date))
                    }
                },
            )
            if (event != pastEvents.last()) {
                HorizontalDivider()
            }
        }
        if (isLoading) {
            item {
                CircularWavyProgressIndicator()
            }

        } else {
            if (isError) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = "could not loaded",
                            tint = LightRed
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            stringResource(R.string.something_went_wrong),
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (pastEvents.isEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "empty"
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            stringResource(R.string.no_past_events),
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (thereIsLastPastEvent) {

                item {
                    Spacer(Modifier.height(5.dp))
                    Column(Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = {
                                onLoadMore()
                            },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .border(
                                    1.dp,
                                    IconButtonDefaults.iconButtonColors().contentColor,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "load more comments"
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun ActiveEventsListPagePreview() {

    val pastEvents = listOf(
        Event(
            title = "etkinlik 1",
            description = "açıklama 1",
            addressDescription = "adres 1",
            privateInfo = true
        ), Event(
            title = "etkinlik 2",
            description = "açıklama 2",
            addressDescription = "adres 2",
            privateInfo = false
        )
    )

    CommunityEventListContent(
        onBackClick = {},
        goToCommunityPage = {},
        scope = rememberCoroutineScope(),
        snackbarHostState = remember { SnackbarHostState() },
        communityName = "community name",
        unlinkEvent = {},
        navigateToEventPage = {},
        navigateToLogIn = {},
        thereIsLastPastEvent = true,
        userResourceFlow = Resource.Success(data = User()),
        getPasEvents = {},
        resetLastPastEvent = {},
        pastEvents = pastEvents,
        myRole = 2,
        pastEventsResource = Resource.Success(data = pastEvents),
        upcomingEventsResoure = Resource.Success(data = pastEvents),
        goToProfile = {},
        tryAgainOnClick = {}
    )
}