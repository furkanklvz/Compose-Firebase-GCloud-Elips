package com.klavs.bindle

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.routes.AppSettings
import com.klavs.bindle.data.routes.Communities
import com.klavs.bindle.data.routes.CommunityEvents
import com.klavs.bindle.data.routes.CommunityPage
import com.klavs.bindle.data.routes.CommunitiesGraph
import com.klavs.bindle.data.routes.CreateCommunity
import com.klavs.bindle.data.routes.CreateEvent
import com.klavs.bindle.data.routes.CreatePost
import com.klavs.bindle.data.routes.CreateUser
import com.klavs.bindle.data.routes.CreateUserPhaseTwo
import com.klavs.bindle.data.routes.EditEvent
import com.klavs.bindle.data.routes.EventChat
import com.klavs.bindle.data.routes.EventPage
import com.klavs.bindle.data.routes.Events
import com.klavs.bindle.data.routes.Greeting
import com.klavs.bindle.data.routes.Home
import com.klavs.bindle.data.routes.Language
import com.klavs.bindle.data.routes.LogIn
import com.klavs.bindle.data.routes.Map
import com.klavs.bindle.data.routes.MapGraph
import com.klavs.bindle.data.routes.Menu
import com.klavs.bindle.data.routes.Post
import com.klavs.bindle.data.routes.Profile
import com.klavs.bindle.data.routes.ResetEmail
import com.klavs.bindle.data.routes.ResetPassword
import com.klavs.bindle.data.routes.SupportAndFeedback
import com.klavs.bindle.data.routes.Theme
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.BindleTheme
import com.klavs.bindle.uix.view.event.Events
import com.klavs.bindle.uix.view.Home
import com.klavs.bindle.uix.view.auth.CreateAccount
import com.klavs.bindle.uix.view.auth.CreateUserSetPassword
import com.klavs.bindle.uix.view.auth.Greeting
import com.klavs.bindle.uix.view.auth.LogIn
import com.klavs.bindle.uix.view.communities.Communities
import com.klavs.bindle.uix.view.communities.CreateCommunity
import com.klavs.bindle.uix.view.communities.communityPage.ActiveEventsListPage
import com.klavs.bindle.uix.view.communities.communityPage.CommunityPage
import com.klavs.bindle.uix.view.communities.communityPage.CreatePost
import com.klavs.bindle.uix.view.communities.communityPage.Post
import com.klavs.bindle.uix.view.event.EditEvent
import com.klavs.bindle.uix.view.event.EventChat
import com.klavs.bindle.uix.view.event.EventPage
import com.klavs.bindle.uix.view.map.CreateEvent
import com.klavs.bindle.uix.view.map.Map
import com.klavs.bindle.uix.view.menu.AppSettings
import com.klavs.bindle.uix.view.menu.LanguageSettings
import com.klavs.bindle.uix.view.menu.Menu
import com.klavs.bindle.uix.view.menu.Profile
import com.klavs.bindle.uix.view.menu.ResetEmail
import com.klavs.bindle.uix.view.menu.ResetPassword
import com.klavs.bindle.uix.view.menu.SupportAndFeedbackPage
import com.klavs.bindle.uix.view.menu.ThemeSettings
import com.klavs.bindle.uix.viewmodel.HomeViewModel
import com.klavs.bindle.uix.viewmodel.LogInViewModel
import com.klavs.bindle.uix.viewmodel.MapViewModel
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.ProfileViewModel
import com.klavs.bindle.uix.viewmodel.SupportAndFeedbackViewmodel
import com.klavs.bindle.uix.viewmodel.communities.CommunityEventListViewModel
import com.klavs.bindle.uix.viewmodel.communities.CommunityPageViewModel
import com.klavs.bindle.uix.viewmodel.communities.CommunityViewModel
import com.klavs.bindle.uix.viewmodel.communities.PostViewModel
import com.klavs.bindle.uix.viewmodel.event.EditEventViewModel
import com.klavs.bindle.uix.viewmodel.event.EventChatViewModel
import com.klavs.bindle.uix.viewmodel.event.EventsViewModel
import com.klavs.bindle.helper.startdestination.StartDestinationProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var startDestinationProvider: StartDestinationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startRoute = startDestinationProvider.determineStartDestination(
            intent
        )

        enableEdgeToEdge()
        setContent {
            BindleTheme {
                NavHostWithBottomNavigation(
                    startRoute = startRoute
                )
            }
        }
    }
}

@Composable
private fun NavHostWithBottomNavigation(
    startRoute: Any? = null
) {
    var privacyPolicyExpanded by remember { mutableStateOf(false) }
    var termsOfServiceExpanded by remember { mutableStateOf(false) }
    var termsOfServiceChecked by remember { mutableStateOf(false) }
    var privacyPolicyChecked by remember { mutableStateOf(false) }
    var termsExpanded by remember { mutableStateOf(false) }
    var successfullyPurchasedProduct by remember { mutableStateOf<String?>(null) }
    var startRouteState by remember { mutableStateOf(startRoute) }
    val navHostViewModel: NavHostViewModel = hiltViewModel()
    val currentUser by navHostViewModel.currentUser.collectAsStateWithLifecycle()
    val userResource by navHostViewModel.userResourceFlow.collectAsStateWithLifecycle()
    val purchaseResource by navHostViewModel.purchase.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var launched by rememberSaveable { mutableStateOf(false) }
    val navController = rememberNavController()
    var bottomBarIsEnable by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(purchaseResource) {
        if (purchaseResource is Resource.Success) {
            successfullyPurchasedProduct = purchaseResource.data!!.products.first()
        }
    }
    LaunchedEffect(currentUser) {
        if (launched) {
            if (currentUser == null) {
                navHostViewModel.listenToUserJob?.cancel()
                navHostViewModel.listenToPurchaseJob?.cancel()
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                navHostViewModel.startToListenUserDocument(
                    uid = currentUser!!.uid
                )
            }
        } else {
            launched = true
            if (currentUser != null) {
                navHostViewModel.startToListenUserDocument(
                    uid = currentUser!!.uid
                )
            }
        }
    }

    LaunchedEffect(userResource) {
        if (userResource is Resource.Success && userResource.data != null) {
            termsExpanded = !userResource.data!!.acceptedTermsAndPrivacyPolicy
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = bottomBarIsEnable) {
                BottomNavigationBar(
                    navController = navController,
                    currentUser = currentUser
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LaunchedEffect(bottomBarIsEnable) {
            Log.e("bottombar", "bottom bar: $bottomBarIsEnable")
        }
        if (successfullyPurchasedProduct != null) {
            PurchaseSuccessfulAlertDialog(
                onDismiss = { successfullyPurchasedProduct = null },
                product = successfullyPurchasedProduct!!
            )
        }
        if (termsOfServiceExpanded) {
            AlertDialog(
                modifier = Modifier.zIndex(3f),
                icon = {
                    Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "Terms of Services")
                },
                onDismissRequest = { termsOfServiceExpanded = false },
                dismissButton = {
                    IconButton(
                        onClick = { termsOfServiceExpanded = false }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "dismiss"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            termsOfServiceChecked = true
                            termsOfServiceExpanded = false
                        }
                    ) {
                        Text(stringResource(R.string.accept))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.terms_of_service_label)
                    )


                },
                text =
                {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.8f)
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.terms_of_service)
                        )
                    }

                }
            )
        }
        if (privacyPolicyExpanded) {
            AlertDialog(
                modifier = Modifier.zIndex(3f),
                icon = {
                    Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "")
                },
                onDismissRequest = { privacyPolicyExpanded = false },
                dismissButton = {
                    IconButton(
                        onClick = { privacyPolicyExpanded = false }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "dismiss"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            privacyPolicyChecked = true
                            privacyPolicyExpanded = false
                        }
                    ) {
                        Text(stringResource(R.string.accept))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.privacy_policy_label)
                    )


                },
                text =
                {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.8f)
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.privacy_policy)
                        )
                    }

                }
            )
        }
        if (termsExpanded) {
            AlertDialog(
                modifier = Modifier.zIndex(2f),
                icon = { Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "") },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                onDismissRequest = {},
                confirmButton = {
                    Button(
                        onClick = {
                            if (privacyPolicyChecked && termsOfServiceChecked && userResource.data != null) {
                                navHostViewModel.acceptTermsAndPrivacyPolicy(
                                    uid = userResource.data!!.uid
                                )
                            }
                        }
                    ) {
                        Text(stringResource(R.string.accept))
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = termsOfServiceChecked,
                                onCheckedChange = { termsOfServiceChecked = it }
                            )
                            val termsOfServiceText = buildAnnotatedString {
                                append(stringResource(R.string.read_and_accept_terms_of_service_append_one))
                                withStyle(
                                    style = SpanStyle(
                                        color = ButtonDefaults.textButtonColors().contentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.terms_of_service_label))
                                }
                                append(stringResource(R.string.read_and_accept_terms_of_service_append_two))
                            }
                            Text(
                                termsOfServiceText,
                                modifier = Modifier.clickable {
                                    termsOfServiceExpanded = true
                                },
                                style = MaterialTheme.typography.bodySmall
                            )


                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = privacyPolicyChecked,
                                onCheckedChange = { privacyPolicyChecked = it }
                            )
                            val privacyPolicyText = buildAnnotatedString {
                                append(stringResource(R.string.read_and_accept_privacy_policy_append_one))
                                withStyle(
                                    style = SpanStyle(
                                        color = ButtonDefaults.textButtonColors().contentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.privacy_policy_label))
                                }
                                append(stringResource(R.string.read_and_accept_privacy_policy_append_two))
                            }
                            Text(
                                privacyPolicyText,
                                modifier = Modifier.clickable {
                                    privacyPolicyExpanded = true
                                },
                                style = MaterialTheme.typography.bodySmall
                            )


                        }
                    }
                }
            )
        }

        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(
                bottom = if (bottomBarIsEnable) innerPadding.calculateBottomPadding()
                else WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            }
        ) {
            composable<Home> {
                LaunchedEffect(true) {
                    bottomBarIsEnable = true
                    startRouteState?.let {
                        startRouteState = null
                        navController.navigate(it)
                    }
                }
                val homeViewModel = hiltViewModel<HomeViewModel>()
                Home(
                    navController = navController,
                    currentUser = currentUser,
                    navHostViewModel = navHostViewModel,
                    homeViewModel = homeViewModel
                )
            }
            navigation<MapGraph>(startDestination = Map) {
                composable<Map> { backStackEntry ->
                    val mapViewModel =
                        backStackEntry.sharedViewModel<MapViewModel>(navController)
                    Map(
                        navController = navController,
                        viewModel = mapViewModel,
                        onBottomBarVisibilityChange = { bottomBarIsEnable = it },
                        currentUser = currentUser,
                        navHostViewModel = navHostViewModel
                    )
                }
                composable<CreateEvent> { backStackEntry ->
                    val mapViewModel = backStackEntry.sharedViewModel<MapViewModel>(navController)
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }
                    val createEvent: CreateEvent = backStackEntry.toRoute()
                    CreateEvent(
                        navController = navController,
                        viewModel = mapViewModel,
                        latitude = createEvent.latitude,
                        longitude = createEvent.longitude,
                        myUid = currentUser!!.uid,
                        navHostViewModel = navHostViewModel
                    )

                }
            }

            composable<Events> {
                LaunchedEffect(true) {
                    bottomBarIsEnable = true
                }
                val viewModel = hiltViewModel<EventsViewModel>(it)
                Events(
                    navController = navController,
                    myUid = currentUser?.uid,
                    navHostViewModel = navHostViewModel,
                    viewModel = viewModel
                )

            }
            composable<Menu> {
                LaunchedEffect(true) {
                    bottomBarIsEnable = true
                }
                Menu(
                    navController = navController,
                    currentUser = currentUser
                )
            }
            composable<LogIn> {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                val viewModel = hiltViewModel<LogInViewModel>(it)
                LogIn(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable<CreateUser> {
                CreateAccount(navController = navController)
            }
            composable<Greeting> {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                Greeting(
                    navController = navController
                )
            }
            composable<CreateUserPhaseTwo> { backStackEntry ->
                val createUserPhaseTwo: CreateUserPhaseTwo = backStackEntry.toRoute()
                val encodedEmail = createUserPhaseTwo.email
                val encodedUsername = createUserPhaseTwo.username
                val decodedEmail = Uri.decode(encodedEmail)
                val decodedUserName = Uri.decode(encodedUsername)
                CreateUserSetPassword(
                    navController = navController,
                    profilePictureUri = createUserPhaseTwo.profilePictureUri,
                    userName = decodedUserName,
                    email = decodedEmail
                )
            }
            composable<Profile> {
                val viewModel = hiltViewModel<ProfileViewModel>(it)
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                Profile(
                    navController = navController,
                    viewModel = viewModel,
                    navHostViewModel = navHostViewModel
                )
            }
            composable<ResetPassword> {
                if (currentUser != null) {
                    val viewModel = hiltViewModel<ProfileViewModel>(it)
                    ResetPassword(
                        navController = navController,
                        currentUser = currentUser!!,
                        viewModel = viewModel,
                    )
                }

            }
            composable<ResetEmail> {
                val viewModel = hiltViewModel<ProfileViewModel>(it)
                ResetEmail(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable<AppSettings> {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                AppSettings(navController = navController)
            }
            composable<Theme> {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                ThemeSettings(navController = navController)
            }
            composable<Language> {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                LanguageSettings(
                    navController = navController
                )
            }
            composable<SupportAndFeedback> {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                val viewModel = hiltViewModel<SupportAndFeedbackViewmodel>(it)
                SupportAndFeedbackPage(
                    navController = navController,
                    currentUser = currentUser,
                    viewmodel = viewModel
                )
            }
            composable<CreateCommunity> {
                if (currentUser != null) {
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }
                    val viewModel = hiltViewModel<CommunityViewModel>(it)
                    CreateCommunity(
                        navController = navController,
                        myUid = currentUser!!.uid,
                        navHostViewModel = navHostViewModel,
                        viewModel = viewModel,
                    )
                }
            }

            composable<EventChat>(
                deepLinks = listOf(navDeepLink {
                    uriPattern = "bindle://event_chat/{eventId}/{numOfParticipants}"
                })
            ) { navBackStackEntry ->
                if (currentUser != null) {
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }
                    val eventChat: EventChat = navBackStackEntry.toRoute()
                    val viewModel = hiltViewModel<EventChatViewModel>(navBackStackEntry)
                    EventChat(
                        navController = navController,
                        eventId = eventChat.eventId,
                        numOfParticipants = eventChat.numParticipants,
                        currentUser = currentUser!!,
                        chatViewModel = viewModel
                    )
                }
            }

            composable<EventPage>(
                deepLinks = listOf(navDeepLink { uriPattern = "bindle://event_page/{eventId}" })
            ) { navBackStackEntry ->
                if (currentUser != null) {
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }
                    val viewModel = hiltViewModel<EventsViewModel>(navBackStackEntry)
                    val eventPage: EventPage = navBackStackEntry.toRoute()
                    EventPage(
                        navController = navController,
                        eventId = eventPage.eventId,
                        currentUser = currentUser!!,
                        navHostViewModel = navHostViewModel,
                        viewModel = viewModel
                    )
                }
            }
            composable<Post>(
                deepLinks = listOf(navDeepLink {
                    uriPattern = "bindle://post/{communityId}/{postId}"
                })
            ) { backStackEntry ->
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                val postViewModel = hiltViewModel<PostViewModel>(backStackEntry)
                val communityPageViewModel =
                    hiltViewModel<CommunityPageViewModel>()
                val post: Post = backStackEntry.toRoute()
                Post(
                    navController = navController,
                    postId = post.postId,
                    viewModel = postViewModel,
                    communityId = post.communityId,
                    currentUser = currentUser!!,
                    communityPageViewModel = communityPageViewModel
                )

            }

            navigation<CommunitiesGraph>(
                startDestination = Communities
            ) {
                composable<Communities> { backStackEntry ->
                    val viewModel = hiltViewModel<CommunityViewModel>(backStackEntry)
                    val postViewModel = backStackEntry.sharedViewModel<PostViewModel>(navController)
                    Communities(
                        navController = navController,
                        currentUser = currentUser,
                        navHostViewModel = navHostViewModel,
                        onBottomBarVisibilityChange = { enable ->
                            bottomBarIsEnable = enable
                            Log.e("communities", "bottom bar: $enable")
                        },
                        viewModel = viewModel,
                        postViewModel = postViewModel
                    )
                }
                composable<CommunityPage>(
                    deepLinks = listOf(navDeepLink {
                        uriPattern = "bindle://community_page/{communityId}"
                    })
                ) { backStackEntry ->
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }
                    val postViewModel = backStackEntry.sharedViewModel<PostViewModel>(navController)
                    val communityPageViewModel =
                        backStackEntry.sharedViewModel<CommunityPageViewModel>(navController)
                    val communityPage: CommunityPage = backStackEntry.toRoute()
                    CommunityPage(
                        navController = navController,
                        vmPost = postViewModel,
                        communityId = communityPage.communityId,
                        currentUser = currentUser,
                        viewModel = communityPageViewModel,
                        navHostViewModel = navHostViewModel
                    )

                }
                composable<CreatePost> { backStackEntry ->
                    if (currentUser != null) {
                        val communityPageViewModel =
                            backStackEntry.sharedViewModel<CommunityPageViewModel>(navController)
                        LaunchedEffect(true) {
                            bottomBarIsEnable = false
                        }
                        val createPost: CreatePost = backStackEntry.toRoute()
                        CreatePost(
                            navController = navController,
                            communityId = createPost.communityId,
                            currentUser = currentUser!!,
                            viewModel = communityPageViewModel
                        )
                    }
                }
                composable<CommunityEvents> { backStackEntry ->
                    if (currentUser != null) {

                        val communityPageViewModel =
                            backStackEntry.sharedViewModel<CommunityPageViewModel>(navController)
                        val eventListViewModel =
                            hiltViewModel<CommunityEventListViewModel>(backStackEntry)
                        val communityEvents: CommunityEvents = backStackEntry.toRoute()
                        val decodedCommunityName =
                            Uri.decode(communityEvents.communityName)
                        ActiveEventsListPage(
                            navController = navController,
                            communityId = communityEvents.communityId,
                            communityName = decodedCommunityName,
                            currentUser = currentUser!!,
                            navHostViewModel = navHostViewModel,
                            eventListViewModel = eventListViewModel,
                            communityPageViewModel = communityPageViewModel
                        )
                    }
                }
            }




            composable<EditEvent> { backStackEntry ->
                if (currentUser != null) {
                    val editEvent: EditEvent = backStackEntry.toRoute()
                    val decodedEventJson = Uri.decode(editEvent.event)
                    val event = Gson().fromJson(decodedEventJson, Event::class.java)
                    val viewModel = hiltViewModel<EditEventViewModel>(backStackEntry)
                    EditEvent(
                        navController = navController,
                        myUid = currentUser!!.uid,
                        event = event,
                        viewModel = viewModel,
                        navHostViewModel = navHostViewModel
                    )
                }
            }

        }
    }
}

@Composable
private inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}

@SuppressLint("SuspiciousIndentation")
@Composable
private fun BottomNavigationBar(navController: NavHostController, currentUser: FirebaseUser?) {
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Communities,
        BottomNavItem.Map,
        BottomNavItem.Events,
        BottomNavItem.Menu
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        bottomNavItems.forEach { bottomBarItem ->
            val selected =
                currentDestination?.hierarchy?.any { it.hasRoute(bottomBarItem.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(
                        bottomBarItem.route
                    ) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = if (bottomBarItem is BottomNavItem.Menu) {
                    {
                        BadgedBox(
                            badge = {
                                if (currentUser?.isEmailVerified == false) {
                                    Badge()
                                }
                            }
                        ) { if (selected) bottomBarItem.selectedIcon() else bottomBarItem.unselectedIcon() }
                    }
                } else {
                    if (selected) bottomBarItem.selectedIcon else bottomBarItem.unselectedIcon
                },
                label = {
                    Text(
                        text = stringResource(bottomBarItem.labelResource),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall
                    )
                })
        }
    }
}

@Composable
private fun PurchaseSuccessfulAlertDialog(
    onDismiss: () -> Unit,
    product: String
) {
    val productLabel = when (product) {
        stringResource(R.string.ten_tickets) -> stringResource(R.string.x_tickets, 10)
        stringResource(R.string.twenty_tickets) -> stringResource(R.string.x_tickets, 20)
        stringResource(R.string.thirty_tickets) -> stringResource(R.string.x_tickets, 30)
        else -> ""
    }

    AlertDialog(
        modifier = Modifier.zIndex(4f),
        icon = {
            Icon(imageVector = Icons.Rounded.Check, contentDescription = "purchased")
        },
        title = {
            Text(stringResource(R.string.purchase_successful))
        },
        text = {
            Text(
                text = stringResource(R.string.x_has_been_loaded_to_your_account, productLabel)
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.great))
            }
        },
        onDismissRequest = onDismiss
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    var privacyPolicyExpanded by remember { mutableStateOf(false) }
    var termsOfServiceExpanded by remember { mutableStateOf(false) }
    var termsOfServiceChecked by remember { mutableStateOf(false) }
    var privacyPolicyChecked by remember { mutableStateOf(false) }
    AlertDialog(
        icon = { Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "") },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = {}
            ) {
                Text(stringResource(R.string.accept))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = termsOfServiceChecked,
                        onCheckedChange = { termsOfServiceChecked = it }
                    )
                    val termsOfServiceText = buildAnnotatedString {
                        append(stringResource(R.string.read_and_accept_terms_of_service_append_one))
                        withStyle(
                            style = SpanStyle(
                                color = ButtonDefaults.textButtonColors().contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(stringResource(R.string.terms_of_service_label))
                        }
                        append(stringResource(R.string.read_and_accept_terms_of_service_append_two))
                    }
                    Text(
                        termsOfServiceText,
                        modifier = Modifier.clickable {
                            termsOfServiceExpanded = true
                        },
                        style = MaterialTheme.typography.bodySmall
                    )


                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = privacyPolicyChecked,
                        onCheckedChange = { privacyPolicyChecked = it }
                    )
                    val privacyPolicyText = buildAnnotatedString {
                        append(stringResource(R.string.read_and_accept_privacy_policy_append_one))
                        withStyle(
                            style = SpanStyle(
                                color = ButtonDefaults.textButtonColors().contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(stringResource(R.string.privacy_policy_label))
                        }
                        append(stringResource(R.string.read_and_accept_privacy_policy_append_two))
                    }
                    Text(
                        privacyPolicyText,
                        modifier = Modifier.clickable {
                            privacyPolicyExpanded = true
                        },
                        style = MaterialTheme.typography.bodySmall
                    )


                }
            }
        }
    )
}