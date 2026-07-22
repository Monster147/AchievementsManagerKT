package pt.jsal.achman.config

import com.fasterxml.jackson.annotation.JsonProperty

data class IntegrationsConfig(
    @get:JsonProperty("STEAM_API_KEY")
    @param:JsonProperty("STEAM_API_KEY")
    val STEAM_API_KEY: String = "",

    @get:JsonProperty("STEAM_USERID")
    @param:JsonProperty("STEAM_USERID")
    val STEAM_USERID: String = "",

    @get:JsonProperty("RETRO_API_KEY")
    @param:JsonProperty("RETRO_API_KEY")
    val RETRO_API_KEY: String = "",

    @get:JsonProperty("RETRO_USERNAME")
    @param:JsonProperty("RETRO_USERNAME")
    val RETRO_USERNAME: String = "",

    @get:JsonProperty("PSN_API_KEY")
    @param:JsonProperty("PSN_API_KEY")
    val PSN_API_KEY: String = "",

    var authTokens: AuthTokensResponse? = null,
    var tokenExpiresAt: Long = 0,
)
