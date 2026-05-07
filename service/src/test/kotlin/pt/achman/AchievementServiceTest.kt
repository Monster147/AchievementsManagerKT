package pt.achman

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.jsal.achman.AchievementError
import pt.jsal.achman.AchievementService
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.user.PasswordValidationInfo
import pt.jsal.achman.user.UserRole
import pt.jsal.achman.utils.Either
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class AchievementServiceTest {
    @Autowired
    private lateinit var achievementService: AchievementService

    @Autowired
    private lateinit var trxManager: TransactionManager

    private fun createAdmin() =
        trxManager.run {
            repoUsers.createUser(
                "admin",
                "admin@mail",
                PasswordValidationInfo("x"),
                UserRole.ADMIN,
            )
        }

    private fun createNormalUser() =
        trxManager.run {
            repoUsers.createUser(
                "user",
                "user@mail",
                PasswordValidationInfo("x"),
                UserRole.NORMAL,
            )
        }

    private fun createGame() =
        trxManager.run {
            repoGames.createGame(
                externalGameId = "game-ext-1",
                name = "Test Game",
                source = GameSource.STEAM,
                cover = "cover.png",
            )
        }

    @BeforeEach
    fun reset() {
        trxManager.run {
            repoAchievements.clear()
            repoUsers.clear()
            repoGames.clear()
        }
    }

    @Test
    fun `createAchievement succeeds for admin`() {
        val admin = createAdmin()
        val game = createGame()

        val result =
            achievementService.createAchievement(
                admin.id,
                game.id,
                apiName = "ach-1",
                name = "First Kill",
                description = "Do something",
                icon = "icon.png",
            )

        assertTrue(result is Either.Right)
        val achievement = result.value

        assertEquals("ach-1", achievement.apiName)
        assertEquals("First Kill", achievement.name)
        assertEquals("icon.png", achievement.icon)
        assertEquals("Do something", achievement.description)
        assertEquals(game.id, achievement.gameId)
    }

    @Test
    fun `createAchievement fails for non admin`() {
        val user = createNormalUser()
        val game = createGame()

        val result =
            achievementService.createAchievement(
                user.id,
                game.id,
                apiName = "ach-1",
                name = "Test",
                description = "Desc",
                icon = "icon.png",
            )

        assertIs<Either.Left<*>>(result)
        assertIs<AchievementError.UserNotAdmin>(result.value)
    }

    @Test
    fun `createAchievement fails if game does not exist`() {
        val admin = createAdmin()

        val result =
            achievementService.createAchievement(
                admin.id,
                gameId = 999,
                apiName = "ach-1",
                name = "Test",
                description = "Desc",
                icon = "icon.png",
            )

        assertIs<Either.Left<*>>(result)
        assertIs<AchievementError.GameNotFound>(result.value)
    }

    @Test
    fun `createAchievement fails if apiName already exists`() {
        val admin = createAdmin()
        val game = createGame()

        achievementService.createAchievement(
            admin.id,
            game.id,
            apiName = "ach-1",
            name = "First",
            description = "Desc",
            icon = "icon.png",
        )

        val result =
            achievementService.createAchievement(
                admin.id,
                game.id,
                apiName = "ach-1",
                name = "Duplicate",
                description = "Desc",
                icon = "icon.png",
            )

        assertIs<Either.Left<*>>(result)
        assertIs<AchievementError.AchievementAlreadyExists>(result.value)
    }

    @Test
    fun `findByGameId returns achievements`() {
        val admin = createAdmin()
        val game = createGame()

        achievementService.createAchievement(admin.id, game.id, "a1", "A1", "D1", "i1")
        achievementService.createAchievement(admin.id, game.id, "a2", "A2", "D2", "i2")

        val result = achievementService.findByGameId(game.id)

        assertEquals(2, result.size)
        assertTrue(result.any { it.apiName == "a1" })
        assertTrue(result.any { it.apiName == "a2" })
    }

    @Test
    fun `findByGameId returns empty list when none`() {
        val result = achievementService.findByGameId(999)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `removeAchievements succeeds for admin`() {
        val admin = createAdmin()
        val game = createGame()

        achievementService.createAchievement(admin.id, game.id, "a1", "A1", "D1", "i1")

        val result = achievementService.removeAchievements(admin.id, game.id)

        assertTrue(result is Either.Right)
        assertTrue(achievementService.findByGameId(game.id).isEmpty())
    }

    @Test
    fun `removeAchievements fails for non admin`() {
        val admin = createAdmin()
        val user = createNormalUser()
        val game = createGame()

        achievementService.createAchievement(admin.id, game.id, "a1", "A1", "D1", "i1")

        val result = achievementService.removeAchievements(user.id, game.id)

        assertIs<Either.Left<*>>(result)
        assertIs<AchievementError.UserNotAdmin>(result.value)
    }

    @Test
    fun `removeAchievements works even if game has no achievements`() {
        val admin = createAdmin()
        val game = createGame()

        val result = achievementService.removeAchievements(admin.id, game.id)

        assertTrue(result is Either.Right)
    }
}
