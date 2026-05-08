package pt.jsal.achman.interfaces

import pt.jsal.achman.config.IntegrationsConfig

interface RepositoryIntegrationConfig {
    fun getConfig(userId: Int): IntegrationsConfig

    fun updateConfig(
        userId: Int,
        config: IntegrationsConfig,
    ): IntegrationsConfig
}
