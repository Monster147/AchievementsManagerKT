package pt.achman

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.jsal.achman.jdbi.TransactionManagerJdbi
import pt.jsal.achman.jdbi.configureWithAppRequirements
import pt.jsal.achman.mem.TransactionManagerInMem
import pt.jsal.achman.token.Sha256TokenEncoder
import pt.jsal.achman.user.UsersDomainConfig
import pt.jsal.achman.jdbi.configureWithAppRequirements
import java.time.Clock
import java.time.Duration

@Configuration
@Profile("mem")
class InMemoryConfig {
    @Bean
    fun trxManager(): TransactionManagerInMem = TransactionManagerInMem()
}

@Configuration
@Profile("jdbi")
class JdbiConfig {
    @Bean
    fun jdbi() =
        Jdbi
            .create(
                PGSimpleDataSource().apply {
                    setURL(Environment.getDbUrl())
                },
            ).configureWithAppRequirements()

    @Bean
    fun trxManagerJdbi(jdbi: Jdbi): TransactionManagerJdbi = TransactionManagerJdbi(jdbi)
}

@SpringBootApplication(scanBasePackages = ["pt.achman"])
class WebApp {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun usersDomainConfig() =
        UsersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = Duration.ofHours(24),
            tokenRollingTtl = Duration.ofHours(1),
            maxTokensPerUser = 3,
        )
}

fun main() {
    runApplication<WebApp>()
}
