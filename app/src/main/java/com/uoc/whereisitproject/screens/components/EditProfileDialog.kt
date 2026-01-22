package com.uoc.whereisitproject.screens.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.uoc.whereisitproject.R

@Composable
fun EditProfileDialog(
    initialFirstName: String,
    initialLastName: String,
    currentImageUrl: String?,
    onDismiss: () -> Unit,
    onSave: (
        firstName: String,
        lastName: String,
        imageUri: Uri?,
        currentPassword: String?,
        newPassword: String?,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) -> Unit
) {
    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }

    var currentPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }

    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var previewImage by remember { mutableStateOf(currentImageUrl) }

    var saving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val fillNameText = stringResource(R.string.needed_name)
    val pwdRequiredText = stringResource(R.string.enter_current_password)
    val newPwdText = stringResource(R.string.enter_new_password)
    val confirmPwdText = stringResource(R.string.enter_confirm_new_password)

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        pickedImageUri = uri
        previewImage = uri?.toString()
    }

    fun validate(): Boolean {
        if (firstName.isBlank() || lastName.isBlank()) {
            errorMsg = fillNameText
            return false
        }

        val wantsPwd = currentPwd.isNotBlank() || newPwd.isNotBlank() || confirmPwd.isNotBlank()

        if (wantsPwd) {
            if (currentPwd.isBlank()) {
                errorMsg = pwdRequiredText; return false
            }
            if (newPwd.isBlank()) {
                errorMsg = newPwdText; return false
            }
            if (newPwd != confirmPwd) {
                errorMsg = confirmPwdText; return false
            }
        }

        errorMsg = null
        return true
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(stringResource(R.string.edit_profile)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(80.dp), shape = CircleShape) {
                        if (previewImage != null) {
                            AsyncImage(previewImage, null, contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Person, null)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                        Text(stringResource(R.string.edit_photo))
                    }
                }

                Text(stringResource(id = R.string.data), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(firstName, { firstName = it }, label = { Text(stringResource(R.string.first_name)) })
                OutlinedTextField(lastName, { lastName = it }, label = { Text(stringResource(R.string.last_name)) })

                HorizontalDivider()
                Text(text = stringResource(id = R.string.change_password), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(currentPwd, { currentPwd = it }, label = { Text(stringResource(R.string.current_password)) }, visualTransformation = PasswordVisualTransformation())
                OutlinedTextField(newPwd, { newPwd = it }, label = { Text(stringResource(R.string.new_password)) }, visualTransformation = PasswordVisualTransformation())
                OutlinedTextField(confirmPwd, { confirmPwd = it }, label = { Text(stringResource(R.string.confirm_new_password)) }, visualTransformation = PasswordVisualTransformation())

                if (errorMsg != null) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray, // background
                    contentColor = Color.White   // text
                ),
                onClick = {
                    if (!validate()) return@Button
                    saving = true
                    onSave(
                        firstName.trim(),
                        lastName.trim(),
                        pickedImageUri,
                        currentPwd.takeIf { it.isNotBlank() },
                        newPwd.takeIf { it.isNotBlank() },
                        { msg ->
                            errorMsg = msg
                            saving = false
                        },
                        {
                            saving = false
                            onDismiss()
                        }
                    )
                }
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(16.dp))
                else Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                enabled = !saving,
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = Color.Black
                )
            }
        }
    )
}