package com.uoc.whereisitproject.model

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.uoc.whereisitproject.repository.changePasswordWithReauth
import com.uoc.whereisitproject.repository.updateProfileImageUrl
import com.uoc.whereisitproject.repository.updateUserNames
import com.uoc.whereisitproject.repository.uploadProfileImageAndGetUrl
import kotlinx.coroutines.launch

@Composable
fun EditProfileDialogAllInOne(
    // initial values for form
    initialFirstName: String,
    initialLastName: String,
    currentImageUrl: String?,
    // dependencies
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    storage: FirebaseStorage,
    // callbacks
    onDismiss: () -> Unit,
    onSaved: () -> Unit // refresh data on profile screen
) {
    // form status
    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName  by remember { mutableStateOf(initialLastName) }

    var currentPwd by remember { mutableStateOf("") }
    var newPwd     by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }

    // Image: preview and URI local if user selects new one
    var previewImageUrl by remember { mutableStateOf(currentImageUrl) }
    var pickedImageUri  by remember { mutableStateOf<Uri?>(null) }

    // UI status
    var saving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val uid = remember { auth.currentUser!!.uid }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedImageUri = uri
            // preview
            previewImageUrl = uri.toString()
        }
    }

    fun validateInputs(): String? {
        // validations
        if (firstName.isBlank() || lastName.isBlank()) return "The First Name or Last Name cannot be empty."
        // If the user wishes to change their password, they must complete all 3 fields and match
        val wantsPasswordChange = currentPwd.isNotBlank() || newPwd.isNotBlank() || confirmPwd.isNotBlank()
        if (wantsPasswordChange) {
            if (currentPwd.isBlank()) return "Enter your current password."
            if (newPwd.isBlank()) return "Enter the new password."
            if (newPwd != confirmPwd) return "The password confirmation does not match."
        }
        return null
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Edit profile") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile image + button to change it
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(80.dp)
                    ) {
                        if (!previewImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = previewImageUrl,
                                contentDescription = "Profile image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        }
                    }
                    OutlinedButton(
                        enabled = !saving,
                        onClick = { imagePicker.launch("image/*") }
                    ) {
                        Text("Edit photo")
                    }
                }

                @Suppress("DEPRECATION")
                Divider()

                // Names
                Text("Data", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )

                @Suppress("DEPRECATION")
                Divider()

                // Password
                Text("Change password", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = currentPwd,
                    onValueChange = { currentPwd = it },
                    label = { Text("Current password") },
                    singleLine = true,
                    enabled = !saving,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text("New password") },
                    singleLine = true,
                    enabled = !saving,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPwd,
                    onValueChange = { confirmPwd = it },
                    label = { Text("Confirm new password") },
                    singleLine = true,
                    enabled = !saving,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                    val validation = validateInputs()
                    if (validation != null) {
                        errorMsg = validation
                        return@Button
                    }
                    errorMsg = null
                    scope.launch {
                        saving = true
                        try {
                            // Upload image if edited
                            pickedImageUri?.let { uri ->
                                val url = uploadProfileImageAndGetUrl(storage, uid, uri)
                                updateProfileImageUrl(db, uid, url)
                            }

                            // Update names if edited
                            if (firstName != initialFirstName || lastName != initialLastName) {
                                updateUserNames(db, uid, firstName.trim(), lastName.trim())
                            }

                            // Change password if asked
                            val wantsPasswordChange = currentPwd.isNotBlank() || newPwd.isNotBlank() || confirmPwd.isNotBlank()
                            if (wantsPasswordChange) {
                                changePasswordWithReauth(auth, currentPwd, newPwd)
                            }

                            // OK
                            onSaved()
                            onDismiss()
                        } catch (e: Exception) {
                            errorMsg = e.message ?: "The changes could not be saved."
                        } finally {
                            saving = false
                        }
                    }
                }
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !saving,
                onClick = onDismiss,
            ) {
                Text(
                    "Cancel",
                    color = Color.Black
                )
            }
        }
    )
}
