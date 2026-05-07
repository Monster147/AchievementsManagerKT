package pt.achman

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.jsal.achman.UserGamesError
import pt.jsal.achman.UserGamesService
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.user.PasswordValidationInfo
import pt.jsal.achman.user.UserRole
import pt.jsal.achman.utils.Either
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class UserGamesServiceTest {
    @Autowired
    private lateinit var service: UserGamesService

    @Autowired
    private lateinit var trxManager: TransactionManager

    private var userId: Int = 0
    private var gameId: Int = 0

    @BeforeEach
    fun setup() {
        trxManager.run {
            repoUserGames.clear()
            repoGames.clear()
            repoUsers.clear()

            userId =
                repoUsers.createUser(
                    "user",
                    "user@mail",
                    PasswordValidationInfo("hash"),
                    UserRole.NORMAL,
                ).id

            gameId =
                repoGames.createGame(
                    "1",
                    "Game",
                    GameSource.STEAM,
                    "cover.jpg",
                ).id
        }
    }

    @Test
    fun `createUserGame returns userGame`() {
        val result = service.createUserGame(userId, gameId)

        assertTrue(result is Either.Right)
        val userGame = result.value

        assertEquals(userId, userGame.userId)
        assertEquals(gameId, userGame.gameId)
    }

    @Test
    fun `createUserGame fails if already exists`() {
        service.createUserGame(userId, gameId)

        val result = service.createUserGame(userId, gameId)

        assertIs<Either.Left<*>>(result)
        assertIs<UserGamesError.UserGameAlreadyExists>(result.value)
    }

    @Test
    fun `createUserGame fails if user does not exist`() {
        val result = service.createUserGame(999, gameId)

        assertIs<Either.Left<*>>(result)
        assertIs<UserGamesError.UserNotFound>(result.value)
    }

    @Test
    fun `createUserGame fails if game does not exist`() {
        val result = service.createUserGame(userId, 999)

        assertIs<Either.Left<*>>(result)
        assertIs<UserGamesError.GameNotFound>(result.value)
    }

    @Test
    fun `findByUserId returns user games`() {
        service.createUserGame(userId, gameId)

        val result = service.findByUserId(userId)

        assertEquals(1, result.size)
        assertEquals(gameId, result.first().gameId)
    }

    @Test
    fun `findByUserId returns empty list when no games`() {
        val result = service.findByUserId(userId)

        assertEquals(emptyList(), result)
    }

    @Test
    fun `findByUserIdAndGameId returns userGame`() {
        service.createUserGame(userId, gameId)

        val result = service.findByUserIdAndGameId(userId, gameId)

        assertTrue(result is Either.Right)
        val userGame = result.value

        assertEquals(userId, userGame.userId)
        assertEquals(gameId, userGame.gameId)
    }

    @Test
    fun `findByUserIdAndGameId fails when not found`() {
        val result = service.findByUserIdAndGameId(userId, gameId)

        assertIs<Either.Left<*>>(result)
        assertIs<UserGamesError.GameNotFound>(result.value)
    }

    @Test
    fun `alterSyncOption toggles sync`() {
        service.createUserGame(userId, gameId)

        val result = service.alterSyncOption(userId, gameId)

        assertTrue(result is Either.Right)
        val updated = result.value

        assertEquals(userId, updated.userId)
        assertEquals(gameId, updated.gameId)
    }

    @Test
    fun `alterSyncOption fails when userGame not found`() {
        val result = service.alterSyncOption(userId, gameId)

        assertIs<Either.Left<*>>(result)
        assertIs<UserGamesError.GameNotFound>(result.value)
    }

    @Test
    fun `alterSyncOption fails if user does not exist`() {
        val result = service.alterSyncOption(999, gameId)

        assertIs<Either.Left<*>>(result)
        assertIs<UserGamesError.UserNotFound>(result.value)
    }

    @Test
    fun `alterSyncOption fails if game does not exist`() {
        val result = service.alterSyncOption(userId, 999)

        assertIs<Either.Left<*>>(result)
        assertIs<UserGamesError.GameNotFound>(result.value)
    }
}
