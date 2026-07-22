package pt.jsal.achman.common

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pt.jsal.achman.config.IntegrationsConfig
import pt.jsal.achman.interfaces.RepositoryIntegrationConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class RepositoryIntegrationsConfigFile(
    private val objectMapper: ObjectMapper,
) : RepositoryIntegrationConfig {
    private val lock = ReentrantLock()

    private fun path(userId: Int): Path = Path.of("config/users/$userId/integrations.json")

    override fun getConfig(userId: Int): IntegrationsConfig =
        lock.withLock {
            val file = path(userId)

            if (!Files.exists(file)) {
                return IntegrationsConfig()
            }

            val json = Files.readString(file)

            val config = objectMapper.readValue(json, IntegrationsConfig::class.java)
            config
        }

    override fun updateConfig(
        userId: Int,
        config: IntegrationsConfig,
    ): IntegrationsConfig =
        lock.withLock {
            val file = path(userId)

            if (!Files.exists(file.parent)) {
                Files.createDirectories(file.parent)
            }

            val json =
                objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(config)

            Files.writeString(
                file,
                json,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
            )

            config
        }
}
