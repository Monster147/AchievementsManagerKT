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
import pt.jsal.achman.model.gameprogress.ClearCompletedAchievements
import pt.jsal.achman.model.gameprogress.CompletedAchievementInput
import pt.jsal.achman.model.gameprogress.CreateGameProgressInput
import pt.jsal.achman.user.AuthenticatedUser
import pt.jsal.achman.utils.Failure
import pt.jsal.achman.utils.Success

@RestController
@RequestMapping("/api/progress")
class GameProgressController(
    private val gameProgressService: GameProgressService,
) {
    @PostMapping("/{gameId}")
    fun createProgress(
        user: AuthenticatedUser,
        @RequestBody input: CreateGameProgressInput,
    ): ResponseEntity<*> {
        val result =
            gameProgressService.createGameProgress(
                user.user.id,
                input.gameId,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    GameProgressError.GameNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("Game not found")

                    GameProgressError.UserNotFound ->
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("User not found")

                    else ->
                        Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @GetMapping("/game/{gameId}/user/{userId}")
    fun findProgressbyGameIdAndUserId(
        @PathVariable gameId: Int,
        @PathVariable userId: Int,
    ): ResponseEntity<*> {
        val result =
            gameProgressService.findByUserIdAndGameId(
                userId,
                gameId,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .ok(result.value)

            is Failure ->
                when (result.value) {
                    GameProgressError.ProgressNotFound ->
                        Problem.GameProgressNotFound.response(HttpStatus.NOT_FOUND)

                    GameProgressError.UserNotFound ->
                        Problem.UserNotFound.response(HttpStatus.NOT_FOUND)

                    GameProgressError.GameNotFound ->
                        Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    else ->
                        Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @GetMapping("/user/{userId}")
    fun findProgressByUserId(
        @PathVariable userId: Int,
    ): ResponseEntity<*> {
        val progressList = gameProgressService.findByUserId(userId)
        return ResponseEntity.ok(progressList)
    }

    @PostMapping("/achievement")
    fun addCompletedAchievement(
        user: AuthenticatedUser,
        @RequestBody input: CompletedAchievementInput,
    ): ResponseEntity<*> {
        val result =
            gameProgressService.addCompletedAchievement(
                input.userId,
                input.gameId,
                input.achievementId,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    GameProgressError.UserNotFound ->
                        Problem.UserNotFound.response(HttpStatus.NOT_FOUND)

                    GameProgressError.GameNotFound ->
                        Problem.GameNotFound.response(HttpStatus.NOT_FOUND)

                    GameProgressError.AchievementNotFound ->
                        Problem.AchievementNotFound.response(HttpStatus.NOT_FOUND)

                    else ->
                        Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @DeleteMapping("/achievement")
    fun removeCompletedAchievement(
        user: AuthenticatedUser,
        @RequestBody input: CompletedAchievementInput,
    ): ResponseEntity<*> {
        val result =
            gameProgressService.removeCompletedAchievement(
                input.userId,
                input.gameId,
                input.achievementId,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    GameProgressError.UserNotFound ->
                        Problem.UserNotFound.response(HttpStatus.NOT_FOUND)

                    GameProgressError.GameNotFound ->
                        Problem.GameNotFound.response(HttpStatus.NOT_FOUND)

                    GameProgressError.AchievementNotFound ->
                        Problem.AchievementNotFound.response(HttpStatus.NOT_FOUND)

                    else ->
                        Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @DeleteMapping
    fun clearCompletedAchievements(
        user: AuthenticatedUser,
        @RequestBody input: ClearCompletedAchievements,
    ): ResponseEntity<*> {
        val result =
            gameProgressService.clearCompletedAchievements(
                input.userId,
                input.gameId,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result.value)

            is Failure ->
                when (result.value) {
                    GameProgressError.UserNotFound ->
                        Problem.UserNotFound.response(HttpStatus.NOT_FOUND)

                    GameProgressError.GameNotFound ->
                        Problem.GameNotFound.response(HttpStatus.NOT_FOUND)

                    GameProgressError.ProgressNotFound ->
                        Problem.GameProgressNotFound.response(HttpStatus.NOT_FOUND)

                    else ->
                        Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }
}
