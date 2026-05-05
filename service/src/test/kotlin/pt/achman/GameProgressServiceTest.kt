package pt.achman

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.achman.game.GameSource
import pt.achman.interfaces.TransactionManager
import pt.achman.user.PasswordValidationInfo
import pt.achman.user.UserRole
import pt.achman.utils.Either
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class GameProgressServiceTest {
    @Autowired
    private lateinit var gameProgressService: GameProgressService

    @Autowired
    private lateinit var trxManager: TransactionManager

    private fun createUser() =
        trxManager.run {
            repoUsers.createUser(
                "user",
                "user@mail",
                PasswordValidationInfo("x"),
                UserRole.NORMAL,
            )
        }

    private fun createAdmin() =
        trxManager.run {
            repoUsers.createUser(
                "admin",
                "admin@mail",
                PasswordValidationInfo("x"),
                UserRole.ADMIN,
            )
        }

    private fun createGame(userId: Int): Int =
        trxManager.run {
            repoGames.createGame(
                externalGameId = "g1",
                name = "Game",
                source = GameSource.STEAM,
            ).id
        }

    private fun createAchievement(gameId: Int): Int =
        trxManager.run {
            repoAchievements.createAchievement(
                apiName = "ach-1",
                name = "A1",
                icon = "icon",
                description = "desc",
                gameId = gameId,
            ).id
        }

    @BeforeEach
    fun reset() {
        trxManager.run {
            repoGameProgress.clear()
            repoAchievements.clear()
            repoGames.clear()
            repoUsers.clear()
        }
    }

    @Test
    fun `createGameProgress succeeds`() {
        val user = createUser()
        val game = createGame(user.id)

        val result = gameProgressService.createGameProgress(user.id, game)

        assertTrue(result is Either.Right)
        val progress = (result as Either.Right).value

        assertEquals(user.id, progress.userId)
        assertEquals(game, progress.gameId)
    }

    @Test
    fun `createGameProgress fails if user not found`() {
        val result = gameProgressService.createGameProgress(999, 1)

        assertIs<Either.Left<*>>(result)
        assertIs<GameProgressError.UserNotFound>(result.value)
    }

    @Test
    fun `createGameProgress fails if game not found`() {
        val user = createUser()

        val result = gameProgressService.createGameProgress(user.id, 999)

        assertIs<Either.Left<*>>(result)
        assertIs<GameProgressError.GameNotFound>(result.value)
    }

    @Test
    fun `findByUserIdAndGameId returns progress`() {
        val user = createUser()
        val game = createGame(user.id)

        val created =
            gameProgressService.createGameProgress(user.id, game).let {
                check(it is Either.Right)
                it.value
            }

        val result = gameProgressService.findByUserIdAndGameId(user.id, game)

        assertTrue(result is Either.Right)
        assertEquals(created.userId, (result as Either.Right).value.userId)
    }

    @Test
    fun `findByUserIdAndGameId fails if not found`() {
        val user = createUser()
        val game = createGame(user.id)

        val result = gameProgressService.findByUserIdAndGameId(user.id, game)

        assertIs<Either.Left<*>>(result)
        assertIs<GameProgressError.ProgressNotFound>(result.value)
    }

    @Test
    fun `findByUserIdAndGameId fails if user missing`() {
        val result = gameProgressService.findByUserIdAndGameId(999, 1)

        assertIs<Either.Left<*>>(result)
        assertIs<GameProgressError.UserNotFound>(result.value)
    }

    @Test
    fun `findByUserIdAndGameId fails if game missing`() {
        val user = createUser()

        val result = gameProgressService.findByUserIdAndGameId(user.id, 999)

        assertIs<Either.Left<*>>(result)
        assertIs<GameProgressError.GameNotFound>(result.value)
    }

    @Test
    fun `findByUserId returns list`() {
        val user = createUser()
        val game = createGame(user.id)

        gameProgressService.createGameProgress(user.id, game)

        val result = gameProgressService.findByUserId(user.id)

        assertEquals(1, result.size)
    }

    @Test
    fun `findByUserId returns empty list`() {
        val user = createUser()

        val result = gameProgressService.findByUserId(user.id)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `addCompletedAchievement succeeds`() {
        val user = createUser()
        val game = createGame(user.id)
        val ach = createAchievement(game)

        gameProgressService.createGameProgress(user.id, game)

        val result = gameProgressService.addCompletedAchievement(user.id, game, ach)

        assertTrue(result is Either.Right)
    }

    @Test
    fun `addCompletedAchievement fails if user not found`() {
        val result = gameProgressService.addCompletedAchievement(999, 1, 1)

        assertIs<Either.Left<*>>(result)
        assertIs<GameProgressError.UserNotFound>(result.value)
    }

    @Test
    fun `addCompletedAchievement fails if game not found`() {
        val user = createUser()

        val result = gameProgressService.addCompletedAchievement(user.id, 999, 1)

        assertIs<Either.Left<*>>(result)
        assertIs<GameProgressError.GameNotFound>(result.value)
    }

    @Test
    fun `addCompletedAchievement fails if achievement not found`() {
        val user = createUser()
        val game = createGame(user.id)

        val result = gameProgressService.addCompletedAchievement(user.id, game, 999)

        assertIs<Either.Left<*>>(result)
        assertIs<GameProgressError.AchievementNotFound>(result.value)
    }

    @Test
    fun `removeCompletedAchievement succeeds`() {
        val user = createUser()
        val game = createGame(user.id)
        val ach = createAchievement(game)

        gameProgressService.createGameProgress(user.id, game)
        gameProgressService.addCompletedAchievement(user.id, game, ach)

        val result = gameProgressService.removeCompletedAchievement(user.id, game, ach)

        assertTrue(result is Either.Right)
    }

    @Test
    fun `clearCompletedAchievements succeeds`() {
        val user = createUser()
        val game = createGame(user.id)

        gameProgressService.createGameProgress(user.id, game)

        val result = gameProgressService.clearCompletedAchievements(user.id, game)

        assertTrue(result is Either.Right)
    }

    @Test
    fun `clearCompletedAchievements fails if progress not found`() {
        val user = createUser()
        val game = createGame(user.id)

        val result = gameProgressService.clearCompletedAchievements(user.id, game)

        assertIs<Either.Left<*>>(result)
        assertIs<GameProgressError.ProgressNotFound>(result.value)
    }
}
