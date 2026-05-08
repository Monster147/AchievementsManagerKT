package pt.jsal.achman

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.jsal.achman.providers.SearchProvider

@RestController
@RequestMapping("/api/searchGames")
class GamesRouter(
    private val searchProvider: SearchProvider,
    private val integrationsConfigService: IntegrationsConfigService,
) {
    /*@PostMapping
    suspend fun searchGames(
        user: AuthenticatedUser,
        @RequestBody searchRequest: SearchGameRequest,
    ): ResponseEntity<*> {
        if (user.user.role != UserRole.ADMIN) return Problem.UserNotAdmin.response(HttpStatus.FORBIDDEN)
        val config = integrationsConfigService.getConfig(user.user.id)
        val searchResult = searchProvider.searchGames(
            config,
            searchRequest.gameName,
            searchRequest.source,
        )

    }*/
}
