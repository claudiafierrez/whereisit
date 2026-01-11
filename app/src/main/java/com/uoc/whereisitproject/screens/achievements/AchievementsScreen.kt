package com.uoc.whereisitproject.screens.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.screens.components.PlaceAchievementsSection

@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel
) {
    val errorLoadAchievementsText =
        stringResource(id = R.string.error_load_achievements)

    LaunchedEffect(Unit) {
        viewModel.loadAchievements(errorLoadAchievementsText)
    }

    when {
        viewModel.loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        viewModel.error != null -> {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.achievements),
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = viewModel.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        else -> {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.achievements),
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(Modifier.height(12.dp))

                if (viewModel.achievements.isEmpty()) {
                    Text(stringResource(id = R.string.no_achievements_yet))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            viewModel.achievements,
                            key = { it.placeId }
                        ) { pa ->
                            PlaceAchievementsSection(pa)
                            HorizontalDivider(Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
