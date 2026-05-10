package pt.jsal.achman.model

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

private const val MEDIA_TYPE = "application/problem+json"
private const val PROBLEM_URI_PATH =
    "https://github.com/Monster147/AchievementsManagerKT/tree/main/docs/problems"

sealed class Problem(
    typeUri: URI,
) {
    val type = typeUri.toString()
    val title = typeUri.toString().split("/").last()

    fun response(status: HttpStatus): ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(this)

    // server
    data object InternalError : Problem(URI("${PROBLEM_URI_PATH}/internal-error"))

    // user
    data object EmailAlreadyInUse : Problem(URI("${PROBLEM_URI_PATH}/email-already-in-use"))

    data object InsecurePassword : Problem(URI("${PROBLEM_URI_PATH}/insecure-password"))

    data object UserOrPasswordAreInvalid : Problem(URI("${PROBLEM_URI_PATH}/user-or-password-are-invalid"))

    data object UserNotAdmin : Problem(URI("${PROBLEM_URI_PATH}/user-not-admin"))

    data object UserNotFound : Problem(URI("${PROBLEM_URI_PATH}/user-not-found"))

    // achievement
    data object AchievementAlreadyExists : Problem(URI("${PROBLEM_URI_PATH}/achievement-already-exists"))

    data object AchievementNotFound : Problem(URI("${PROBLEM_URI_PATH}/achievement-not-found"))

    // game
    data object GameNotFound : Problem(URI("${PROBLEM_URI_PATH}/game-not-found"))

    data object GameAlreadyExists : Problem(URI("${PROBLEM_URI_PATH}/game-already-exists"))

    // gameProgress
    data object GameProgressNotFound : Problem(URI("${PROBLEM_URI_PATH}/game-progress-not-found"))

    // userGames
    data object UserGameAlreadyExists : Problem(URI("${PROBLEM_URI_PATH}/user-game-already-exists"))

    // gameSearch
    data object NoGameFound : Problem(URI("${PROBLEM_URI_PATH}/no-game-found"))

    // getAchievement
    data object NoAchievementFound : Problem(URI("${PROBLEM_URI_PATH}/no-achievement-found"))
}
