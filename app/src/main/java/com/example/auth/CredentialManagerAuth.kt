package com.example.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class CredentialManagerAuth(private val auth: FirebaseAuth) {
  suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
    return try {
      val credentialManager = CredentialManager.create(activityContext)
      val webClientId = activityContext.getString(R.string.default_web_client_id)
      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(false)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(activityContext, request)
      val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
      val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
      val authResult = auth.signInWithCredential(firebaseCred).await()
      val user = authResult.user ?: return Result.failure(IllegalStateException("Google sign-in returned no user."))
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
    val cleanEmail = email.trim()
    if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
      return Result.failure(IllegalArgumentException("Enter a valid email address."))
    }
    if (password.length < 6) {
      return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
    }
    return try {
      val result = auth.signInWithEmailAndPassword(cleanEmail, password).await()
      val user = result.user ?: return Result.failure(IllegalStateException("Sign-in returned no user."))
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signUpWithEmail(displayName: String, email: String, password: String): Result<FirebaseUser> {
    val cleanName = displayName.trim().ifEmpty { "Shopper" }.take(80)
    val cleanEmail = email.trim()
    if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
      return Result.failure(IllegalArgumentException("Enter a valid email address."))
    }
    if (password.length < 6) {
      return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
    }
    return try {
      val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
      val user = result.user ?: return Result.failure(IllegalStateException("Signup returned no user."))
      val profileUpdate = UserProfileChangeRequest.Builder()
        .setDisplayName(cleanName)
        .build()
      runCatching { user.updateProfile(profileUpdate).await() }
      runCatching { user.sendEmailVerification().await() }
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun sendPasswordReset(email: String): Result<Unit> {
    val cleanEmail = email.trim()
    if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
      return Result.failure(IllegalArgumentException("Enter your account email first."))
    }
    return try {
      auth.sendPasswordResetEmail(cleanEmail).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun resendVerificationEmail(): Result<Unit> {
    val user = auth.currentUser ?: return Result.failure(IllegalStateException("Not signed in."))
    return try {
      user.sendEmailVerification().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun signOut() {
    auth.signOut()
  }
}
