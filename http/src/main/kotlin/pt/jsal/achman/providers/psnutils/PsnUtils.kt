package pt.jsal.achman.providers.psnutils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.future.await
import pt.jsal.achman.config.AuthTokensResponse
import pt.jsal.achman.config.IntegrationsConfig
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

private const val PSN_AUTH_BASE_URL = "https://ca.account.sony.com/api/authz/v3/oauth"

const val PSN_TROPHY_BASE_URL = "https://m.np.playstation.com/api/trophy"
private const val CLIENT_ID = "09515159-7237-4370-9b40-3806e67c0891"
private const val CLIENT_SECRET_BASIC =
    "Basic MDk1MTUxNTktNzIzNy00MzcwLTliNDAtMzgwNmU2N2MwODkxOnVjUGprYTV0bnRCMktxc1A="

suspend fun exchangeNpssoForAccessCode(
    npssoToken: String,
    client: HttpClient,
): String {
    val queryString =
        listOf(
            "access_type" to "offline",
            "client_id" to CLIENT_ID,
            "redirect_uri" to "com.scee.psxandroid.scecompcall://redirect",
            "response_type" to "code",
            "scope" to "psn:mobile.v2.core psn:clientapp",
        ).joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }

    val requestUrl = "$PSN_AUTH_BASE_URL/authorize?$queryString"

    val request =
        HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .header("Cookie", "npsso=$npssoToken")
            .GET()
            .build()

    val response =
        client.sendAsync(
            request,
            HttpResponse.BodyHandlers.ofString(),
        ).await()

    val locationHeader =
        response.headers()
            .firstValue("location")
            .orElse(null)

    if (locationHeader == null || !locationHeader.contains("?code=")) {
        error(
            "There was a problem retrieving your PSN access code. Is your NPSSO code valid?\n" +
                "To get a new NPSSO code, visit https://ca.account.sony.com/api/v1/ssocookie.",
        )
    }

    val redirectPart = locationHeader.substringAfter("redirect/?")

    val redirectParams =
        redirectPart.split("&").associate {
            val (key, value) = it.split("=", limit = 2)

            key to
                URLDecoder.decode(
                    value,
                    StandardCharsets.UTF_8,
                )
        }

    return redirectParams["code"]
        ?: error("Code parameter not found in redirect URL")
}

suspend fun exchangeAccessCodeForToken(
    accessCode: String,
    client: HttpClient,
): AuthTokensResponse {
    val requestUrl = "$PSN_AUTH_BASE_URL/token"

    val formBody =
        formData(
            "code" to accessCode,
            "redirect_uri" to "com.scee.psxandroid.scecompcall://redirect",
            "grant_type" to "authorization_code",
            "token_format" to "jwt",
        )

    val request =
        HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", CLIENT_SECRET_BASIC)
            .POST(HttpRequest.BodyPublishers.ofString(formBody))
            .build()

    val response =
        client.sendAsync(
            request,
            HttpResponse.BodyHandlers.ofString(),
        ).await()

    return parseAuthTokensResponse(response.body())
}

suspend fun exchangeRefreshTokenForAuthTokens(
    refreshToken: String,
    client: HttpClient,
): AuthTokensResponse {
    val requestUrl = "$PSN_AUTH_BASE_URL/token"

    val formBody =
        formData(
            "refresh_token" to refreshToken,
            "grant_type" to "refresh_token",
            "token_format" to "jwt",
            "scope" to "psn:mobile.v2.core psn:clientapp",
        )

    val request =
        HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", CLIENT_SECRET_BASIC)
            .POST(HttpRequest.BodyPublishers.ofString(formBody))
            .build()

    val response =
        client.sendAsync(
            request,
            HttpResponse.BodyHandlers.ofString(),
        ).await()

    return parseAuthTokensResponse(response.body())
}

private fun formData(vararg pairs: Pair<String, String>): String =
    pairs.joinToString("&") { (key, value) ->
        "${encode(key)}=${encode(value)}"
    }

private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

private fun parseAuthTokensResponse(json: String): AuthTokensResponse {
    val mapper = jacksonObjectMapper()

    val raw: Map<String, Any> =
        mapper.readValue(
            json,
            mapper.typeFactory.constructMapType(
                Map::class.java,
                String::class.java,
                Any::class.java,
            ),
        )

    return AuthTokensResponse(
        accessToken = raw["access_token"] as String,
        expiresIn = (raw["expires_in"] as Number).toInt(),
        idToken = raw["id_token"] as String,
        refreshToken = raw["refresh_token"] as String,
        refreshTokenExpiresIn = (raw["refresh_token_expires_in"] as Number).toInt(),
        scope = raw["scope"] as String,
        tokenType = raw["token_type"] as String,
    )
}

suspend fun authenticate(
    config: IntegrationsConfig,
    client: HttpClient,
) {
    val authCode = exchangeNpssoForAccessCode(config.PSN_API_KEY, client)
    val tokens = exchangeAccessCodeForToken(authCode, client)

    config.authTokens = tokens
    config.tokenExpiresAt = System.currentTimeMillis() + tokens.expiresIn * 1000
}
