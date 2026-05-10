package pt.jsal.achman.config

data class AuthTokensResponse(
    /** Used to retrieve data from the PSN API. */
    val accessToken: String,
    /** When the access token will expire. */
    val expiresIn: Int,
    val idToken: String,
    /** Used to retrieve a new access token when it expires. */
    val refreshToken: String,
    /** When the refresh token will expire. */
    val refreshTokenExpiresIn: Int,
    val scope: String,
    val tokenType: String,
)
