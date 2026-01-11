package com.uoc.whereisitproject.screens.social

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.screens.components.NotificationsIcon
import com.uoc.whereisitproject.screens.components.PendingRequestsDialog
import com.uoc.whereisitproject.screens.components.UserSearchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    viewModel: SocialViewModel,
    onUserClick: (String) -> Unit
) {
    val searchErrorText = stringResource(id = R.string.search_error)

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = stringResource(id = R.string.social),
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                actions = {
                    NotificationsIcon(
                        pendingCount = viewModel.pendingCount,
                        onClick = { viewModel.openRequestsDialog() }
                    )
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            @Suppress("DEPRECATION")
            SearchBar(
                query = viewModel.query,
                onQueryChange = { viewModel.onQueryChange(it, searchErrorText) },
                onSearch = { viewModel.search(searchErrorText) },
                active = viewModel.isSearchActive,
                onActiveChange = { viewModel.onSearchActiveChange(it) },
                placeholder = {
                    Text(stringResource(id = R.string.search_users_by_username))
                },
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Magnifying glass")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                when {
                    viewModel.loading -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    viewModel.error != null -> {
                        Text(
                            text = viewModel.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    viewModel.results.isEmpty() && viewModel.query.isNotBlank() -> {
                        Text(
                            text = stringResource(R.string.matching_users) + " “${viewModel.query}”.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(viewModel.results, key = { it.userId }) { user ->
                                UserSearchItem(user) {
                                    onUserClick(user.userId)
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        PendingRequestsDialog(
            visible = viewModel.isDialogOpen,
            requests = viewModel.pendingRequests,
            onAccept = viewModel::acceptFollow,
            onReject = viewModel::rejectFollow,
            onDismiss = viewModel::closeRequestsDialog
        )
    }
}