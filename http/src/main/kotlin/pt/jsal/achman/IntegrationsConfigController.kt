package pt.jsal.achman

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.jsal.achman.model.config.UpdateIntegrationsConfigInput
import pt.jsal.achman.user.AuthenticatedUser

@RestController
@RequestMapping("/api/configs")
class IntegrationsConfigController(
    private val integrationsConfigService: IntegrationsConfigService,
    private val objectMapper: ObjectMapper,
) {
    @GetMapping
    fun getConfig(user: AuthenticatedUser): ResponseEntity<*> {
        println(objectMapper)
        val config = integrationsConfigService.getConfig(user.user.id)
        println("Objeto: " + objectMapper.writeValueAsString(config))
        return ResponseEntity.ok(config)
    }

    @PostMapping
    fun updateConfig(
        user: AuthenticatedUser,
        @RequestBody config: UpdateIntegrationsConfigInput,
    ): ResponseEntity<*> {
        val config =
            integrationsConfigService.updateConfig(
                user.user.id,
                config.steamApiKey,
                config.steamUserId,
                config.retroApiKey,
                config.retroUsername,
                config.psnApiKey,
            )
        return ResponseEntity.ok(config)
    }
}
