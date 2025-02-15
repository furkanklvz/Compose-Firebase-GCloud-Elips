package com.klavs.bindle.uix.view.event

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.message.Message
import com.klavs.bindle.data.routes.EventPage
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green2
import com.klavs.bindle.uix.view.CoilImageLoader
import com.klavs.bindle.uix.viewmodel.event.EventChatViewModel
import com.klavs.bindle.helper.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventChat(
    eventId: String,
    numOfParticipants: Int,
    currentUser: FirebaseUser,
    navController: NavHostController,
    chatViewModel: EventChatViewModel
) {
    val eventResource by chatViewModel.event.collectAsStateWithLifecycle()
    val pagedMessagesResource by chatViewModel.messages.collectAsStateWithLifecycle()
    val newMessagesResource by chatViewModel.newMessages.collectAsStateWithLifecycle()
    val messageSentResource by chatViewModel.messageSent.collectAsStateWithLifecycle()
    val messages = remember { mutableStateListOf<Message>() }
    var messagesLoaded by remember { mutableStateOf(false) }
    var reportUserDialog by remember { mutableStateOf<Message?>(null) }


    BackHandler {
        if (navController.currentBackStackEntry == null) {
            navController.navigate(EventPage(eventId))
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(true) {
        if (numOfParticipants == -1) {
            chatViewModel.getNumOfParticipant(eventId = eventId)
        }
        chatViewModel.listenToEvent(
            eventId = eventId
        )
        Constants.displayedChatId = eventId
    }
    DisposableEffect(Unit) {
        onDispose {
            Constants.displayedChatId = null
        }
    }

    LaunchedEffect(eventResource) {
        if (eventResource is Resource.Success && eventResource.data != null) {
            if (pagedMessagesResource is Resource.Idle) {
                chatViewModel.getMessages(
                    eventId = eventId,
                    pageSize = 10,
                    myUid = currentUser.uid
                )
            }
        }
    }

    LaunchedEffect(pagedMessagesResource.data) {
        if (pagedMessagesResource is Resource.Success && pagedMessagesResource.data != null) {
            messages.addAll(pagedMessagesResource.data!!.sortedByDescending { it.timestamp as? Timestamp })
            messagesLoaded = true
        }
    }

    EventChatContent(
        newMessagesResource = newMessagesResource,
        messageSentResource = messageSentResource,
        numOfParticipantsText = "${if (numOfParticipants == -1) chatViewModel.numOfParticipant.value ?: "" else numOfParticipants}",
        uid = currentUser.uid,
        pagedMessagesResource = pagedMessagesResource,
        eventResource = eventResource,
        changeChatRestriction = { checked ->
            chatViewModel.changeChatRestriction(
                eventId = eventId,
                restriction = checked
            )
        },
        onBackClick = {
            if (navController.currentBackStackEntry == null) {
                navController.navigate(EventPage(eventId))
            } else {
                navController.popBackStack()
            }
        },
        reportUserDialog = reportUserDialog,
        setReportUserDialog = { reportUserDialog = it },
        sendReport = { reportType, description ->
            chatViewModel.sendReport(
                reportedUser = reportUserDialog?.senderUid ?: "",
                uid = currentUser.uid,
                reportType = reportType,
                description = description,
                messageContent = reportUserDialog?.message ?: "",
                messageId = reportUserDialog?.id ?: ""
            )
        },
        messagesLoaded = messagesLoaded,
        setMessagesLoaded = { messagesLoaded = it },
        messages = messages,
        thereIsOldestMessageDoc = chatViewModel.oldestMessageDoc != null,
        sendMessage = {
            chatViewModel.sendMessage(
                eventId = eventResource.data?.id ?: "",
                message = it,
                username = currentUser.displayName ?: ""
            )
        },
        getMessages = {
            chatViewModel.getMessages(
                eventId = eventResource.data?.id ?: "",
                pageSize = 10,
                myUid = currentUser.uid
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventChatContent(
    newMessagesResource: Resource<List<Message>>,
    pagedMessagesResource: Resource<List<Message>>,
    eventResource: Resource<Event>,
    messageSentResource: Resource<Message>,
    numOfParticipantsText: String,
    messagesLoaded: Boolean,
    thereIsOldestMessageDoc: Boolean,
    setMessagesLoaded: (Boolean) -> Unit,
    changeChatRestriction: (checked: Boolean) -> Unit,
    onBackClick: () -> Unit,
    getMessages: () -> Unit,
    messages: List<Message>,
    reportUserDialog: Message?,
    setReportUserDialog: (Message?) -> Unit,
    sendMessage: (Message) -> Unit,
    sendReport: (content: Int, description: String) -> Unit,
    uid: String
) {
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.imePadding()) },
        topBar = {
            TopAppBar(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                ),
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Group,
                            contentDescription = "count the participants"
                        )
                        Text(numOfParticipantsText)
                    }
                    if (eventResource.data?.ownerUid == uid) {
                        var expandChatSettings by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { expandChatSettings = true }
                        ) {
                            Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "more")
                        }
                        DropdownMenu(
                            expanded = expandChatSettings,
                            onDismissRequest = { expandChatSettings = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.participants_cannot_message)) },
                                onClick = {},
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    Switch(
                                        thumbContent = {
                                            if (eventResource.data.chatRestriction) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Lock,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                                )
                                            }
                                        },
                                        checked = eventResource.data.chatRestriction,
                                        onCheckedChange = { checked ->
                                            changeChatRestriction(checked)
                                        }
                                    )
                                }
                            )
                        }

                    }
                },
                title = {
                    Text(
                        eventResource.data?.title ?: "", style = MaterialTheme.typography.titleSmall
                    )

                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(
                    top = innerPadding.calculateTopPadding()
                )
                .fillMaxSize()

        ) {

            if (reportUserDialog != null) {
                var selectedContent by remember { mutableStateOf<Int?>(null) }
                var optionalDescription by remember { mutableStateOf("") }
                val messageLimit = 100
                Dialog(onDismissRequest = { setReportUserDialog(null) }) {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.report_the_user),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.please_select_one) + ":",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.Start)
                            )
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 0 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.child_abuse_or_illegal_content),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 0) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "sexual content"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 1 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.disruptive_behavior_or_harassment),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 1) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "violence"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 2 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.something_else),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 2) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "something else"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(
                                thickness = 3.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth(0.2f)
                                    .clip(CircleShape)
                            )
                            TextField(
                                label = {
                                    Text(
                                        stringResource(R.string.optional_description),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                value = optionalDescription,
                                minLines = 1,
                                modifier = Modifier.fillMaxWidth(0.9f),
                                supportingText = {
                                    Text("$messageLimit/${optionalDescription.length}")
                                },
                                maxLines = 4,
                                textStyle = TextStyle(
                                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                ),
                                onValueChange = {
                                    if (it.length <= messageLimit) {
                                        optionalDescription = it
                                    }
                                }
                            )
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { setReportUserDialog(null) }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "back"
                                    )
                                }
                                OutlinedButton(
                                    enabled = selectedContent != null,
                                    onClick = {
                                        sendReport(selectedContent!!, optionalDescription)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.report_sent)
                                            )
                                        }
                                        setReportUserDialog(null)
                                    }
                                ) {
                                    Text(stringResource(R.string.send))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "send",
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(IconButtonDefaults.xSmallIconSize)
                                    )
                                }
                            }

                        }
                    }
                }
            }
            if (isLoading) {
                CircularWavyProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .zIndex(2f)
                )
            }
            when (eventResource) {
                is Resource.Error -> {
                    isLoading = false
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(imageVector = Icons.Rounded.WarningAmber, contentDescription = "error")
                        Text(
                            eventResource.messageResource?.let { stringResource(it) } ?: "",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                is Resource.Idle -> {}

                is Resource.Loading -> {
                    isLoading = true
                }

                is Resource.Success -> {
                    when (newMessagesResource) {
                        is Resource.Error -> {
                            isLoading = false
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = "error"
                                )
                                Text(
                                    newMessagesResource.messageResource?.let { stringResource(it) }
                                        ?: "",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }

                        is Resource.Idle -> {}

                        is Resource.Loading -> {
                            isLoading = true
                        }

                        is Resource.Success -> {
                            if (pagedMessagesResource is Resource.Success) {
                                setMessagesLoaded(true)
                            }
                            if (messagesLoaded) {
                                isLoading = false
                                Column(
                                    Modifier
                                        .fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (messages.isEmpty() && (newMessagesResource.data
                                            ?: emptyList()).isEmpty()
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            Text(
                                                if (eventResource.data?.ownerUid != uid && eventResource.data?.chatRestriction == true) {
                                                    stringResource(R.string.no_messages)
                                                } else {
                                                    stringResource(R.string.start_a_conversation)
                                                },
                                                style = MaterialTheme.typography.titleLarge,
                                                modifier = Modifier.align(
                                                    Alignment.Center
                                                )
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            reverseLayout = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            items(newMessagesResource.data!!) { message ->
                                                val index =
                                                    newMessagesResource.data.indexOf(message)
                                                MessageRow(
                                                    message = message,
                                                    eventOwnerUid = eventResource.data?.ownerUid
                                                        ?: "",
                                                    reportUser = { setReportUserDialog(message) },
                                                    uid = uid,
                                                    isFirstMessageOfUser = if (index == newMessagesResource.data.size - 1) true else
                                                        newMessagesResource.data[index + 1].senderUid
                                                                != message.senderUid


                                                )
                                            }

                                                items(messages) { message ->
                                                    val index =
                                                        messages.indexOf(message)
                                                    MessageRow(
                                                        message = message,
                                                        eventOwnerUid = eventResource.data?.ownerUid
                                                            ?: "",
                                                        reportUser = { setReportUserDialog(message) },
                                                        uid = uid,
                                                        isFirstMessageOfUser = if (index == messages.size - 1) true else
                                                            messages[index + 1].senderUid
                                                                    != message.senderUid
                                                    )
                                                }



                                            if (pagedMessagesResource is Resource.Loading) {
                                                item {
                                                    Box(
                                                        Modifier
                                                            .padding(15.dp)
                                                            .fillMaxWidth()
                                                    ) {
                                                        CircularWavyProgressIndicator(
                                                            Modifier
                                                                .size(40.dp)
                                                                .align(
                                                                    Alignment.Center
                                                                )
                                                        )
                                                    }

                                                }
                                            } else {
                                                if (thereIsOldestMessageDoc && eventResource.data?.id != null) {
                                                    item {
                                                        Box(
                                                            Modifier
                                                                .padding(15.dp)
                                                                .fillMaxWidth()
                                                        ) {
                                                            IconButton(
                                                                onClick = getMessages,
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .align(Alignment.Center)
                                                                    .border(
                                                                        1.dp,
                                                                        IconButtonDefaults.iconButtonColors().contentColor,
                                                                        CircleShape
                                                                    )
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.Add,
                                                                    contentDescription = "load more"
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (eventResource.data?.chatRestriction == true && uid != eventResource.data.ownerUid) {
                                        Surface(
                                            modifier = Modifier.padding(5.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                stringResource(R.string.only_event_owner_can_message),
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(5.dp)
                                            )
                                        }
                                    } else {
                                        val focusRequester = remember { FocusRequester() }
                                        Row(
                                            modifier = Modifier
                                                .consumeWindowInsets(innerPadding)
                                                .imePadding()
                                                .padding(5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextField(
                                                placeholder = { Text(stringResource(R.string.event_chat_message_placeholder)) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .focusRequester(focusRequester),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = TextFieldDefaults.colors(
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent,
                                                    disabledIndicatorColor = Color.Transparent
                                                ),
                                                value = messageText,
                                                maxLines = 4,
                                                onValueChange = { messageText = it }
                                            )
                                            IconButton(
                                                enabled = messageText.isNotBlank() && messageSentResource !is Resource.Loading,
                                                onClick = {
                                                    val messageModel = Message(
                                                        message = messageText.trim(),
                                                        senderUid = uid,
                                                        timestamp = FieldValue.serverTimestamp()
                                                    )
                                                    sendMessage(messageModel)
                                                    messageText = ""

                                                },
                                                modifier = Modifier
                                                    .padding(start = 5.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        CircleShape
                                                    )
                                            ) {
                                                if (messageSentResource is Resource.Loading) {
                                                    CircularWavyProgressIndicator(
                                                        Modifier.size(
                                                            IconButtonDefaults.smallIconSize
                                                        )
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Rounded.ArrowUpward,
                                                        contentDescription = "send message"
                                                    )
                                                }
                                            }
                                            LaunchedEffect(true) {
                                                focusRequester.requestFocus()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun MessageRow(
    message: Message,
    eventOwnerUid: String,
    uid: String,
    isFirstMessageOfUser: Boolean,
    reportUser: () -> Unit
) {
    val context = LocalContext.current
    val isMyMessage = message.senderUid == uid
    val isEventOwner = eventOwnerUid == message.senderUid
    var sentYesterday = false

    val sdf: SimpleDateFormat? = if (message.timestamp == null) {
        null
    } else if (Timestamp.now()
            .toDate().time - ((message.timestamp as? Timestamp)?.toDate()?.time
            ?: 0L) < TimeUnit.DAYS.toMillis(
            1
        )
    ) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    } else {
        if (Timestamp.now()
                .toDate().time - ((message.timestamp as? Timestamp)?.toDate()?.time
                ?: 0L) < TimeUnit.DAYS.toMillis(2)
        ) {
            sentYesterday = true
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else if (Timestamp.now()
                .toDate().time - ((message.timestamp as? Timestamp)?.toDate()?.time
                ?: 0L) < TimeUnit.DAYS.toMillis(365)
        ) {
            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("dd MMM yy, HH:mm", Locale.getDefault())
        }
    }
    Column(Modifier.fillMaxWidth()) {
        if (isMyMessage) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 10.dp)
                    .align(Alignment.End)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(
                            bottomEnd = 1.dp,
                            bottomStart = 13.dp,
                            topEnd = 13.dp,
                            topStart = 13.dp
                        ),
                        color = MaterialTheme.colorScheme.surfaceTint
                    ) {
                        Text(message.message, modifier = Modifier.padding(10.dp))
                    }
                    if (sdf != null) {
                        Text(
                            "${if (sentYesterday) "${stringResource(R.string.yesterday)}, " else ""}${
                                sdf.format(
                                    (message.timestamp as? Timestamp)?.toDate() ?: Date(0)
                                )
                            }",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

        } else {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 10.dp)
                    .clickable { reportUser() }
            ) {
                if (isFirstMessageOfUser) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        if (message.senderPhotoUrl != null) {
                            CoilImageLoader(
                                message.senderPhotoUrl,
                                context = context,
                                Modifier.matchParentSize(),
                                message.senderUsername?:"message sender"
                            )
                        } else {
                            Image(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "user",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.LightGray)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(top = 30.dp)) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 1.dp,
                                bottomEnd = 13.dp,
                                topEnd = 13.dp,
                                bottomStart = 13.dp
                            ),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    start = 10.dp,
                                    end = 10.dp,
                                    top = 7.dp,
                                    bottom = 10.dp
                                )
                            ) {
                                if (isEventOwner) {
                                    Text(
                                        stringResource(R.string.event_owner),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Green2.copy(red = 0.2f, blue = 0.2f, green = 0.4f),
                                        modifier = Modifier
                                            .background(Green2, RoundedCornerShape(6.dp))
                                            .padding(1.dp)
                                    )
                                }
                                Text(
                                    message.senderUsername ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    message.message,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                        }
                        if (sdf != null) {
                            Text(
                                "${if (sentYesterday) "${stringResource(R.string.yesterday)}, " else ""}${
                                    sdf.format(
                                        (message.timestamp as? Timestamp)?.toDate() ?: Date(0)
                                    )
                                }",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.Start)
                            )

                        }

                    }
                } else {
                    Box(
                        Modifier
                            .width(40.dp)
                            .clip(CircleShape)
                    ) {

                    }
                    Column {
                        Surface(
                            shape = RoundedCornerShape(
                                bottomStart = 13.dp,
                                bottomEnd = 13.dp,
                                topEnd = 13.dp,
                                topStart = 13.dp
                            ),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    start = 10.dp,
                                    end = 10.dp,
                                    top = 7.dp,
                                    bottom = 10.dp
                                )
                            ) {
                                /*if (isEventOwner) {
                                    Text(
                                        stringResource(R.string.event_owner),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Green2.copy(red = 0.2f, blue = 0.2f, green = 0.4f),
                                        modifier = Modifier
                                            .background(Green2, RoundedCornerShape(6.dp))
                                            .padding(1.dp)
                                    )
                                }
                                Text(
                                    message.senderUsername ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )*/
                                Text(
                                    message.message,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                        }
                        if (sdf != null) {
                            Text(
                                "${if (sentYesterday) "${stringResource(R.string.yesterday)}, " else ""}${
                                    sdf.format(
                                        (message.timestamp as? Timestamp)?.toDate() ?: Date(0)
                                    )
                                }",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(bottom = 2.dp)
                            )

                        }

                    }
                }

            }
        }
    }


}


@Preview(showSystemUi = true)
@Composable
private fun EventChatPreview() {
    val newMessagesData = listOf(
        Message(
            senderUid = "arif",
            senderUsername = "arif",
            message = "mesaj 1",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 26).toInstant(ZoneOffset.UTC))
        ),
        Message(
            senderUid = "arif",
            senderUsername = "arif",
            message = "mesaj 2",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 25).toInstant(ZoneOffset.UTC))
        ),
        Message(
            senderUid = "0",
            senderUsername = "0",
            message = "mesaj 3",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 24).toInstant(ZoneOffset.UTC))
        ),
        Message(
            senderUid = "0",
            senderUsername = "0",
            message = "mesaj 4",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 23).toInstant(ZoneOffset.UTC))
        ),
        Message(
            senderUid = "arif",
            senderUsername = "arif",
            message = "mesaj 6",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 22).toInstant(ZoneOffset.UTC))
        ),
        Message(
            senderUid = "arif",
            senderUsername = "arif",
            message = "mesaj 7",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 21).toInstant(ZoneOffset.UTC))
        ),
        Message(
            senderUid = "arif",
            senderUsername = "arif",
            message = "mesaj 8",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 20).toInstant(ZoneOffset.UTC))
        ),
        Message(
            senderUid = "berkan",
            senderUsername = "berkan",
            message = "mesaj 9",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 19).toInstant(ZoneOffset.UTC))
        ),
        Message(
            senderUid = "cemile",
            senderUsername = "cemile",
            message = "mesaj 10",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 18).toInstant(ZoneOffset.UTC))
        ),
        Message(
            senderUid = "cemile",
            senderUsername = "cemile",
            message = "mesaj 11",
            timestamp = Timestamp(LocalDateTime.of(2025, 1, 6, 20, 17).toInstant(ZoneOffset.UTC))
        )
    )

    val eventData = Event(
        title = "event"
    )

    EventChatContent(
        newMessagesResource = Resource.Success(data = newMessagesData),
        pagedMessagesResource = Resource.Idle(),
        eventResource = Resource.Success(data = eventData),
        messageSentResource = Resource.Idle(),
        numOfParticipantsText = "20",
        messagesLoaded = true,
        thereIsOldestMessageDoc = true,
        setMessagesLoaded = {},
        changeChatRestriction = {},
        onBackClick = {},
        getMessages = {},
        messages = emptyList(),
        reportUserDialog = null,
        setReportUserDialog = {},
        sendMessage = {},
        sendReport = { _, _ -> },
        uid = "0"
    )

}