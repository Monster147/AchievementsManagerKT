package pt.jsal.achman

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.jsal.achman.http.AuthenticatedUserArgumentResolver
import pt.jsal.achman.http.AuthenticationInterceptor
import pt.jsal.achman.jdbi.TransactionManagerJdbi
import pt.jsal.achman.jdbi.configureWithAppRequirements
import pt.jsal.achman.mem.TransactionManagerInMem
import pt.jsal.achman.token.Sha256TokenEncoder
import pt.jsal.achman.user.UsersDomainConfig
import java.net.http.HttpClient
import java.time.Clock
import java.time.Duration

@Configuration
class PipelineConfigurer(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

@Configuration
@Profile("mem")
class InMemoryConfig {
    @Bean
    fun trxManager(objectMapper: ObjectMapper): TransactionManagerInMem = TransactionManagerInMem(objectMapper)
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
    fun trxManagerJdbi(jdbi: Jdbi, objectMapper: ObjectMapper): TransactionManagerJdbi = TransactionManagerJdbi(jdbi, objectMapper)
}

@Configuration
class HttpClientConfig {
    @Bean
    fun httpClient(): HttpClient =
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_2)
            .build()
}

@SpringBootApplication(scanBasePackages = ["pt.jsal.achman"])
class WebApp {
    @Bean
    fun objectMapper(): ObjectMapper =
        jacksonObjectMapper().findAndRegisterModules()

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
