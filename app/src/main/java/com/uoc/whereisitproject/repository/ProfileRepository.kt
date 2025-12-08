package com.uoc.whereisitproject.repository

import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

// update firstName and lastName (merge)
suspend fun updateUserNames(
    db: FirebaseFirestore, uid: String, firstName: String, lastName: String
) {
    db.collection("users").document(uid)
        .set(mapOf("firstName" to firstName, "lastName" to lastName), SetOptions.merge())
        .await()
}

// change password (reauthentication with actual password required)
suspend fun changePasswordWithReauth(
    auth: FirebaseAuth, currentPassword: String, newPassword: String
) {
    val user = auth.currentUser ?: error("No user")
    val email = user.email ?: error("User has no email")
    val credential = EmailAuthProvider.getCredential(email, currentPassword)
    user.reauthenticate(credential).await()
    user.updatePassword(newPassword).await()
}

// upload profile image and return download URL
suspend fun uploadProfileImageAndGetUrl(
    storage: FirebaseStorage, uid: String, localUri: Uri
): String {
    val ref = storage.reference.child("profileImages/$uid.jpg")
    ref.putFile(localUri).await()
    return ref.downloadUrl.await().toString()
}

// update the image URL in Firestore
suspend fun updateProfileImageUrl(
    db: FirebaseFirestore, uid: String, url: String
) {
    db.collection("users").document(uid)
        .set(mapOf("profileImageUrl" to url), SetOptions.merge())
        .await()
}