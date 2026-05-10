package pt.jsal.achman

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.jsal.achman.model.Problem
import pt.jsal.achman.model.gamesearch.SearchGameRequest
import pt.jsal.achman.providers.SearchError
import pt.jsal.achman.providers.SearchProvider
import pt.jsal.achman.user.AuthenticatedUser
import pt.jsal.achman.user.UserRole
import pt.jsal.achman.utils.Failure
import pt.jsal.achman.utils.Success

@RestController
@RequestMapping("/api/searchGames")
class GameSearchRouter(
    private val searchProvider: SearchProvider,
    private val integrationsConfigService: IntegrationsConfigService,
) {
    @PostMapping
    suspend fun searchGames(
        user: AuthenticatedUser,
        @RequestBody searchRequest: SearchGameRequest,
    ): ResponseEntity<*> {
        if (user.user.role != UserRole.ADMIN) return Problem.UserNotAdmin.response(HttpStatus.FORBIDDEN)
        val config = integrationsConfigService.getConfig(user.user.id)
        val searchResult =
            searchProvider.searchGames(
                user.user.id,
                config,
                searchRequest.gameName,
                searchRequest.source,
            )
        return when (searchResult) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(searchResult.value)

            is Failure ->
                when (searchResult.value) {
                    is SearchError.NoGameFound ->
                        Problem.NoGameFound.response(HttpStatus.NOT_FOUND)
                    else ->
                        Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @PostMapping("/cache")
    fun getCache(): ResponseEntity<*> = ResponseEntity.ok(searchProvider.getCache())
}
