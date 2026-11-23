package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CredentialManagerAuth(private val auth: FirebaseAuth) {

  suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
    return try {
      val credentialManager = CredentialManager.create(activityContext)
      val webClientId = activityContext.getString(R.string.default_web_client_id)
      val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInWithGoogleOption)
        .build()

      val result = credentialManager.getCredential(activityContext, request)
      val credential = result.credential
      if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
      ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        val authResult = auth.signInWithCredential(firebaseCred).await()
        val user = authResult.user
          ?: return Result.failure(IllegalStateException("Google sign-in returned no user."))
        Result.success(user)
      } else {
        Result.failure(IllegalStateException("Unsupported credential returned by Credential Manager."))
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "User cancelled Google sign-in", e)
      Result.failure(IllegalStateException("Google sign-in cancelled."))
    } catch (e: NoCredentialException) {
      Log.w("Auth", "No Google credentials found on device", e)
      Result.failure(
        IllegalStateException(
          "No Google account credential found on this device. Add a Google account in Settings or continue in Guest Mode.",
        ),
      )
    } catch (e: GetCredentialException) {
      Log.e("Auth", "Credential Manager error during Google sign-in", e)
      Result.failure(
        IllegalStateException(
          e.message ?: "Google sign-in unavailable. You can still use the scanner in Guest Mode.",
        ),
      )
    } catch (e: Exception) {
      Log.e("Auth", "Firebase sign-in failed", e)
      Result.failure(e)
    }
  }

  suspend fun attemptAutoSignIn(activityContext: Context): FirebaseUser? {
    if (auth.currentUser != null) return auth.currentUser
    return try {
      val credentialManager = CredentialManager.create(activityContext)
      val webClientId = activityContext.getString(R.string.default_web_client_id)
      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(activityContext, request)
      val credential = result.credential
      if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
      ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        auth.signInWithCredential(firebaseCred).await().user
      } else {
        null
      }
    } catch (e: CancellationException) {
      throw e
    } catch (_: NoCredentialException) {
      null
    } catch (_: GetCredentialCancellationException) {
      null
    } catch (_: Exception) {
      null
    }
  }

  suspend fun signOut(context: Context) {
    auth.signOut()
    withContext(NonCancellable) {
      runCatching {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
      }.onFailure { err ->
        Log.w("Auth", "Failed to clear credential state on sign-out", err)
      }
    }
  }
}
