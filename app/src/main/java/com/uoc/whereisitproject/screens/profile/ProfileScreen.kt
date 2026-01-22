package com.uoc.whereisitproject.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.screens.components.AvatarHeader
import com.uoc.whereisitproject.screens.components.EditProfileDialog
import com.uoc.whereisitproject.screens.components.UserInfoSection

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val errorProfileSavingText = stringResource(id = R.string.error_saving_data)
    val errorProfileLoadingText = stringResource(id = R.string.profile_error_loading)

    LaunchedEffect(Unit) {
        viewModel.load(errorProfileLoadingText)
    }

    when {
        viewModel.loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

        viewModel.profile != null -> {
            val p = viewModel.profile!!

            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.profile), style = MaterialTheme.typography.headlineLarge)

                AvatarHeader(p.profileImageUrl, p.username)

                HorizontalDivider()

                UserInfoSection(
                    firstName = p.firstName,
                    lastName = p.lastName,
                    email = p.email,
                    points = p.points,
                    showEmail = true
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 12.dp,
                        alignment = Alignment.CenterHorizontally
                    )
                ) {
                    Button(
                        onClick = viewModel::openEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray, // background
                            contentColor = Color.White   // text
                        )
                    ) {
                        Text(stringResource(R.string.edit_profile))
                    }
                    OutlinedButton(onClick = viewModel::logout) {
                        Text(stringResource(R.string.logout))
                    }
                }
            }

            if (viewModel.showEditDialog) {
                EditProfileDialog(
                    initialFirstName = p.firstName,
                    initialLastName = p.lastName,
                    currentImageUrl = p.profileImageUrl,
                    onDismiss = viewModel::closeEdit,
                    onSave = { first, last, img, curPwd, newPwd, onErr, onOk ->
                        viewModel.saveProfile(
                            firstName = first,
                            lastName = last,
                            newImage = img,
                            currentPwd = curPwd,
                            newPwd = newPwd,
                            onError = onErr,
                            onSuccess = onOk,
                            errorLoadingText = errorProfileLoadingText,
                            errorSavingText = errorProfileSavingText
                        )
                    }
                )
            }
        }
    }
}