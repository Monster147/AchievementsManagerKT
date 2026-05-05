package pt.achman

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.achman.game.GameGenre
import pt.achman.game.GamePlatform
import pt.achman.game.GameSource
import pt.achman.interfaces.TransactionManager
import pt.achman.user.PasswordValidationInfo
import pt.achman.user.UserRole
import pt.achman.utils.Either
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class GameServiceTest {
    @Autowired
    private lateinit var gameService: GameService

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

    @BeforeEach
    fun reset() {
        trxManager.run {
            repoGames.clear()
            repoUsers.clear()
        }
    }

    @Test
    fun `createGame succeeds for admin`() {
        val admin = createAdmin()

        val result =
            gameService.createGame(
                admin.id,
                "123",
                "Game",
                GameSource.STEAM,
            )

        assertTrue(result is Either.Right)
        val game = result.value

        assertEquals("123", game.externalGameId)
        assertEquals("Game", game.name)
        assertEquals(GameSource.STEAM, game.source)
    }

    @Test
    fun `createGame fails for non admin`() {
        val user = createNormalUser()

        val result =
            gameService.createGame(
                user.id,
                "123",
                "Game",
                GameSource.STEAM,
            )

        assertIs<Either.Left<*>>(result)
        assertIs<GameError.UserNotAdmin>(result.value)
    }

    @Test
    fun `createGame fails if already exists`() {
        val admin = createAdmin()

        gameService.createGame(admin.id, "123", "Game", GameSource.STEAM)

        val result = gameService.createGame(admin.id, "123", "Game", GameSource.STEAM)

        assertIs<Either.Left<*>>(result)
        assertIs<GameError.GameAlreadyExists>(result.value)
    }

    @Test
    fun `findByExternalId returns game`() {
        val admin = createAdmin()

        val created =
            gameService.createGame(admin.id, "123", "Game", GameSource.STEAM).let {
                check(it is Either.Right)
                it.value
            }

        val found =
            gameService.findByExternalId("123", GameSource.STEAM).let {
                check(it is Either.Right)
                it.value
            }

        assertEquals(created.id, found.id)
    }

    @Test
    fun `findByExternalId fails when not found`() {
        val result = gameService.findByExternalId("999", GameSource.STEAM)

        assertIs<Either.Left<*>>(result)
        assertIs<GameError.GameNotFound>(result.value)
    }

    @Test
    fun `findById returns game`() {
        val admin = createAdmin()

        val created =
            gameService.createGame(admin.id, "123", "Game", GameSource.STEAM).let {
                check(it is Either.Right)
                it.value
            }

        val found =
            gameService.findById(created.id).let {
                check(it is Either.Right)
                it.value
            }

        assertEquals(created.id, found.id)
    }

    @Test
    fun `findById fails when not found`() {
        val result = gameService.findById(999)

        assertIs<Either.Left<*>>(result)
        assertIs<GameError.GameNotFound>(result.value)
    }

    @Test
    fun `findAll returns all games`() {
        val admin = createAdmin()

        val g1 = gameService.createGame(admin.id, "1", "G1", GameSource.STEAM)
        val g2 = gameService.createGame(admin.id, "2", "G2", GameSource.PSN)

        val all = gameService.findAll()

        assertEquals(2, all.size)
    }

    @Test
    fun `updateGameInfo updates all fields`() {
        val admin = createAdmin()

        val game =
            gameService.createGame(admin.id, "123", "Old", GameSource.STEAM).let {
                check(it is Either.Right)
                it.value
            }

        val updated =
            gameService.updateGameInfo(
                admin.id,
                game.id,
                "999",
                "New",
                listOf(GameGenre.ACTION),
                GamePlatform.PC,
                "2024",
                GameSource.PSN,
                "cover.jpg",
            ).let {
                check(it is Either.Right)
                it.value
            }

        assertEquals("999", updated.externalGameId)
        assertEquals("New", updated.name)
        assertEquals(listOf(GameGenre.ACTION), updated.genre)
        assertEquals(GamePlatform.PC, updated.platform)
        assertEquals("2024", updated.releaseYear)
        assertEquals(GameSource.PSN, updated.source)
        assertEquals("cover.jpg", updated.cover)
    }

    @Test
    fun `updateGameInfo fails for non admin`() {
        val admin = createAdmin()
        val user = createNormalUser()

        val game =
            gameService.createGame(admin.id, "123", "Game", GameSource.STEAM).let {
                check(it is Either.Right)
                it.value
            }

        val result =
            gameService.updateGameInfo(
                user.id,
                game.id,
                null, null, null, null, null, null, null,
            )

        assertIs<Either.Left<*>>(result)
        assertIs<GameError.UserNotAdmin>(result.value)
    }

    @Test
    fun `updateGameInfo fails if game not found`() {
        val admin = createAdmin()

        val result =
            gameService.updateGameInfo(
                admin.id,
                999,
                null, null, null, null, null, null, null,
            )

        assertIs<Either.Left<*>>(result)
        assertIs<GameError.GameNotFound>(result.value)
    }

    @Test
    fun `deleteById removes game`() {
        val admin = createAdmin()

        val game =
            gameService.createGame(admin.id, "123", "Game", GameSource.STEAM).let {
                check(it is Either.Right)
                it.value
            }

        val result = gameService.deleteById(admin.id, game.id)

        assertTrue(result is Either.Right)

        val found = gameService.findById(game.id)
        assertIs<Either.Left<*>>(found)
    }

    @Test
    fun `deleteById fails for non admin`() {
        val admin = createAdmin()
        val user = createNormalUser()

        val game =
            gameService.createGame(admin.id, "123", "Game", GameSource.STEAM).let {
                check(it is Either.Right)
                it.value
            }

        val result = gameService.deleteById(user.id, game.id)

        assertIs<Either.Left<*>>(result)
        assertIs<GameError.UserNotAdmin>(result.value)
    }

    @Test
    fun `deleteById fails if game not found`() {
        val admin = createAdmin()

        val result = gameService.deleteById(admin.id, 999)

        assertIs<Either.Left<*>>(result)
        assertIs<GameError.GameNotFound>(result.value)
    }
}
