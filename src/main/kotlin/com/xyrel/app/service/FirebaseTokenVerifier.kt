package com.xyrel.app.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.xyrel.app.common.InternalException
import com.xyrel.app.common.UnauthorizedException
import jakarta.annotation.PostConstruct
import java.io.ByteArrayInputStream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FirebaseTokenVerifier(
    @param:Value("\${" + "firebase.type}") private val type: String,
    @param:Value("\${" + "firebase.project-id}") private val projectId: String,
    @param:Value("\${" + "firebase.private-key-id}") private val privateKeyId: String,
    @param:Value("\${" + "firebase.private-key}") private val privateKey: String,
    @param:Value("\${" + "firebase.client-email}") private val clientEmail: String,
    @param:Value("\${" + "firebase.client-id}") private val clientId: String,
    @param:Value("\${" + "firebase.auth-uri}") private val authUri: String,
    @param:Value("\${" + "firebase.token-uri}") private val tokenUri: String,
    @param:Value("\${" + "firebase.auth-provider-cert-url}")
    private val authProviderCertUrl: String,
    @param:Value("\${" + "firebase.client-cert-url}") private val clientCertUrl: String,
    @param:Value("\${" + "firebase.universe-domain}") private val universeDomain: String,
) {
  private val log = LoggerFactory.getLogger(FirebaseTokenVerifier::class.java)

  @PostConstruct
  fun initialize() {
    try {
      if (FirebaseApp.getApps().isNotEmpty()) return

      val serviceAccountJson = buildServiceAccountJson()
      val credentials =
          GoogleCredentials.fromStream(ByteArrayInputStream(serviceAccountJson.toByteArray()))
      val options =
          FirebaseOptions.builder().setCredentials(credentials).setProjectId(projectId).build()

      FirebaseApp.initializeApp(options)
      log.info("Firebase Admin SDK initialized for project: $projectId")
    } catch (e: Exception) {
      log.error("Failed to initialize Firebase: ${e.message}")
      throw InternalException("Firebase initialization failed: ${e.message}")
    }
  }

  fun verifyIdToken(idToken: String): FirebaseToken {
    return try {
      FirebaseAuth.getInstance().verifyIdToken(idToken)
    } catch (e: Exception) {
      log.warn("Firebase token verification failed: ${e.message}")
      throw UnauthorizedException("Invalid Firebase ID token")
    }
  }

  private fun buildServiceAccountJson(): String {
    val normalizedKey = privateKey.replace("\\n", "\n").replace("\r", "").trim().trim('"')

    val jsonEscapedKey = normalizedKey.replace("\n", "\\n")

    return """
        {
          "type": "$type",
          "project_id": "$projectId",
          "private_key_id": "$privateKeyId",
          "private_key": "$jsonEscapedKey",
          "client_email": "$clientEmail",
          "client_id": "$clientId",
          "auth_uri": "$authUri",
          "token_uri": "$tokenUri",
          "auth_provider_x509_cert_url": "$authProviderCertUrl",
          "client_x509_cert_url": "$clientCertUrl",
          "universe_domain": "$universeDomain"
        }
    """
        .trimIndent()
  }
}
