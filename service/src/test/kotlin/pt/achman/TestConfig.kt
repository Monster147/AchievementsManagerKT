package pt.achman

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.jsal.achman.mem.TransactionManagerInMem
import pt.jsal.achman.token.Sha256TokenEncoder
import pt.jsal.achman.user.UsersDomainConfig
import java.time.Clock
import java.time.Duration

@Configuration
@ComponentScan("pt.jsal.achman")
class TestConfig {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun trxManager() = TransactionManagerInMem()

    @Bean
    fun usersDomainConfig() =
        UsersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = Duration.ofHours(24),
            tokenRollingTtl = Duration.ofHours(1),
            maxTokensPerUser = 3,
        )
}
