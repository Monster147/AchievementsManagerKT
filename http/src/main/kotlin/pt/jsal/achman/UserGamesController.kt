package pt.jsal.achman

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.jsal.achman.model.Problem
import pt.jsal.achman.model.usergames.UserGameInput
import pt.jsal.achman.user.AuthenticatedUser
import pt.jsal.achman.utils.Failure
import pt.jsal.achman.utils.Success

@RestController
@RequestMapping("/api/library")
class UserGamesController(
    private val userGamesService: UserGamesService,
) {
    @PostMapping
    fun createUserGame(
        user: AuthenticatedUser,
        @RequestBody input: UserGameInput,
    ): ResponseEntity<*> {
        val result =
            userGamesService.createUserGame(
                user.user.id,
                input.gameId,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    is UserGamesError.GameNotFound -> Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    is UserGamesError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    is UserGamesError.UserGameAlreadyExists -> Problem.UserGameAlreadyExists.response(HttpStatus.CONFLICT)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @GetMapping("/{userId}")
    fun getUserGamesByUserId(
        @PathVariable userId: Int,
    ): ResponseEntity<*> {
        val result = userGamesService.findByUserId(userId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/user/{userId}/game/{gameId}")
    fun getUserGamesByUserIdAndGameId(
        @PathVariable userId: Int,
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val result = userGamesService.findByUserIdAndGameId(userId, gameId)
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    is UserGamesError.GameNotFound -> Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    is UserGamesError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @PostMapping("/user/game/{gameId}/updateSync")
    fun alterSyncOption(
        user: AuthenticatedUser,
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val result =
            userGamesService.alterSyncOption(
                user.user.id,
                gameId,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    is UserGamesError.GameNotFound -> Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    is UserGamesError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @DeleteMapping("/user")
    fun removeUserGames(user: AuthenticatedUser): ResponseEntity<*> {
        val result = userGamesService.removeUserGames(user.user.id)
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Unit>()

            is Failure ->
                when (result.value) {
                    is UserGamesError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @DeleteMapping("/user/game/{gameId}")
    fun removeGame(
        user: AuthenticatedUser,
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val result =
            userGamesService.removeGame(
                user.user.id,
                gameId,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Unit>()

            is Failure ->
                when (result.value) {
                    is UserGamesError.GameNotFound -> Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    is UserGamesError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }
}
