package com.example.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete"),
  LIST("list"),
  GET("get"),
  WRITE("write"),
}

class FirestoreStructuredException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

object FirestoreErrorHandler {
  fun handleFirestoreError(
    error: Throwable,
    operationType: OperationType,
    path: String?,
    auth: FirebaseAuth,
  ): Nothing {
    val user = auth.currentUser
    val providerArray = JSONArray()
    user?.providerData?.forEach { info ->
      providerArray.put(
        JSONObject().apply {
          put("providerId", info.providerId)
          put("email", info.email)
        },
      )
    }

    val authJson = JSONObject().apply {
      put("userId", user?.uid)
      put("email", user?.email)
      put("emailVerified", user?.isEmailVerified)
      put("isAnonymous", user?.isAnonymous)
      put("tenantId", user?.tenantId)
      put("providerInfo", providerArray)
    }

    val payload = JSONObject().apply {
      put("error", error.message ?: error.toString())
      put("operationType", operationType.value)
      put("path", path)
      put("authInfo", authJson)
    }

    val jsonString = payload.toString()
    Log.e("FirestoreErrorHandler", "Firestore Error: $jsonString")
    throw FirestoreStructuredException(jsonString, error)
  }
}
