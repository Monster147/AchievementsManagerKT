package pt.jsal.achman

import org.springframework.stereotype.Component
import pt.jsal.achman.config.IntegrationsConfig
import pt.jsal.achman.interfaces.TransactionManager

@Component
class IntegrationsConfigService(
    private val trxManager: TransactionManager,
) {
    fun getConfig(userId: Int): IntegrationsConfig =
        trxManager.run {
            repoConfig.getConfig(userId)
        }

    fun updateConfig(
        userId: Int,
        steamApiKey: String?,
        steamUserId: String?,
        retroApiKey: String?,
        retroUsername: String?,
        psnApiKey: String?,
    ): IntegrationsConfig =
        trxManager.run {
            val current = repoConfig.getConfig(userId)

            val updated =
                current.copy(
                    STEAM_API_KEY = steamApiKey ?: current.STEAM_API_KEY,
                    STEAM_USERID = steamUserId ?: current.STEAM_USERID,
                    RETRO_API_KEY = retroApiKey ?: current.RETRO_API_KEY,
                    RETRO_USERNAME = retroUsername ?: current.RETRO_USERNAME,
                    PSN_API_KEY = psnApiKey ?: current.PSN_API_KEY,
                )

            repoConfig.updateConfig(userId, updated)
        }
}
