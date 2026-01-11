package com.uoc.whereisitproject.screens.social

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.screens.components.UserInfoSection
import com.uoc.whereisitproject.screens.components.AvatarHeader
import com.uoc.whereisitproject.screens.components.FollowChip
import com.uoc.whereisitproject.screens.components.PlaceAchievementsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialProfileScreen(
    userId: String,
    viewModel: SocialProfileViewModel,
    navController: NavHostController
) {
    val errorText = stringResource(R.string.profile_error_loading)

    LaunchedEffect(userId) {
        viewModel.load(userId, errorText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = stringResource(id = R.string.profile),
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        when {
            viewModel.loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            viewModel.error != null -> {
                Text(
                    viewModel.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            viewModel.profile != null -> {
                val profile = viewModel.profile!!

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    AvatarHeader(profile.profileImageUrl, profile.username)

                    FollowChip(
                        status = viewModel.followStatus,
                        loading = false,
                        onFollow = { viewModel.follow(userId) },
                        onUnfollow = { viewModel.unfollow(userId) },
                        onPendingClick = { viewModel.cancelRequest(userId) }
                    )

                    HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp))

                    if (viewModel.followStatus?.status == "accepted") {
                        UserInfoSection(
                            firstName = profile.firstName,
                            lastName = profile.lastName,
                            email = profile.email,
                            points = profile.points,
                            showEmail = false
                        )

                        HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp))

                        Text(text = stringResource(id = R.string.achievements), style = MaterialTheme.typography.headlineSmall)
                        if (viewModel.achievements.isEmpty()) {
                            Text(text = stringResource(id = R.string.no_achievements_yet))
                        } else {
                            LazyColumn (
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(viewModel.achievements, key = { it.placeId }) {
                                    PlaceAchievementsSection(it)
                                    HorizontalDivider(Modifier.padding(30.dp,8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}