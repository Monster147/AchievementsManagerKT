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
import pt.jsal.achman.model.achievement.CreateAchievementInput
import pt.jsal.achman.user.AuthenticatedUser
import pt.jsal.achman.utils.Failure
import pt.jsal.achman.utils.Success

@RestController
@RequestMapping("/api/achievements")
class AchievementController(
    private val achievementService: AchievementService,
) {
    @PostMapping
    fun createAchievement(
        user: AuthenticatedUser,
        @RequestBody createAchievementInput: CreateAchievementInput,
    ): ResponseEntity<*> {
        val result =
            achievementService.createAchievement(
                user.user.id,
                createAchievementInput.gameId,
                createAchievementInput.apiName,
                createAchievementInput.name,
                createAchievementInput.description,
                createAchievementInput.icon,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(
                        "Location",
                        "/api/achievements/${createAchievementInput.gameId}",
                    ).build<Unit>()

            is Failure ->
                when (result.value) {
                    is AchievementError.UserNotAdmin ->
                        Problem.UserNotAdmin.response(HttpStatus.FORBIDDEN)
                    is AchievementError.GameNotFound ->
                        Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    is AchievementError.AchievementAlreadyExists ->
                        Problem.AchievementAlreadyExists.response(HttpStatus.BAD_REQUEST)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @GetMapping("/{gameId}")
    fun findByGameId(
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val result = achievementService.findByGameId(gameId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result)
    }

    @DeleteMapping("/{gameId}")
    fun removeAchievements(
        user: AuthenticatedUser,
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val result =
            achievementService.removeAchievements(
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
                    is AchievementError.UserNotAdmin ->
                        Problem.UserNotAdmin.response(HttpStatus.BAD_REQUEST)
                    is AchievementError.GameNotFound ->
                        Problem.GameNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }
}
