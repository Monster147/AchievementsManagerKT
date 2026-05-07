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
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.model.Problem
import pt.jsal.achman.model.game.CreateGameInput
import pt.jsal.achman.model.game.UpdateGameInput
import pt.jsal.achman.user.AuthenticatedUser
import pt.jsal.achman.utils.Failure
import pt.jsal.achman.utils.Success

@RestController
@RequestMapping("/games")
class GameController(
    private val gameService: GameService,
) {
    @PostMapping
    fun createGame(
        user: AuthenticatedUser,
        @RequestBody input: CreateGameInput,
    ): ResponseEntity<*> {
        val result =
            gameService.createGame(
                user.user.id,
                input.externalGameId,
                input.name,
                input.source,
            )

        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    is GameError.UserNotAdmin ->
                        Problem.UserNotAdmin.response(HttpStatus.FORBIDDEN)
                    is GameError.GameAlreadyExists ->
                        Problem.GameAlreadyExists.response(HttpStatus.CONFLICT)
                    else ->
                        Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @GetMapping("/externalId/{externalId}/source/{source}")
    fun findByExternalId(
        @PathVariable externalId: String,
        @PathVariable source: GameSource,
    ): ResponseEntity<*> {
        val result =
            gameService.findByExternalId(
                externalId,
                source,
            )
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    is GameError.GameNotFound ->
                        Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @PostMapping("/update/{gameId}")
    fun updateGame(
        user: AuthenticatedUser,
        @PathVariable gameId: Int,
        @RequestBody input: UpdateGameInput,
    ): ResponseEntity<*> {
        val result =
            gameService.updateGameInfo(
                user.user.id,
                gameId,
                input.externalGameId,
                input.name,
                input.genres,
                input.platform,
                input.releaseYear,
                input.source,
                input.cover,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    is GameError.GameNotFound -> Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    is GameError.UserNotAdmin -> Problem.UserNotAdmin.response(HttpStatus.FORBIDDEN)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @GetMapping("/{gameId}")
    fun getGameById(
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val result = gameService.findById(gameId)
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    is GameError.GameNotFound -> Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @GetMapping
    fun findAll(): ResponseEntity<*> {
        val result = gameService.findAll()
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/{gameId}")
    fun deleteGame(
        user: AuthenticatedUser,
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val result =
            gameService.deleteById(
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
                    is GameError.GameNotFound -> Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    is GameError.UserNotAdmin -> Problem.UserNotAdmin.response(HttpStatus.FORBIDDEN)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }
}
