package pt.jsal.achman

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.jsal.achman.model.Problem
import pt.jsal.achman.user.AuthenticatedUser
import pt.jsal.achman.utils.Failure
import pt.jsal.achman.utils.Success

@RestController
@RequestMapping("/api/stats")
class UserStatsController(
    private val userStatsService: UserStatsService,
) {
    @GetMapping("/{userId}")
    fun getUserStats(user: AuthenticatedUser, @PathVariable userId: String): ResponseEntity<*> {
        val id = userId.toIntOrNull() ?: return Problem.InvalidRequestContent.response(HttpStatus.BAD_REQUEST)
        val result = userStatsService.getUserStats(id)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> when (result.value) {
                is UserStatsError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }
}