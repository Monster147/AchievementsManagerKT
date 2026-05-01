package pt.achman.mem

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.achman.interfaces.RepositoryUserGames
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryUserGamesMemTest {
    private lateinit var repo: RepositoryUserGames

    @BeforeEach
    fun setup() {
        repo = RepositoryUserGamesMem()
    }

    @Test
    fun `createUserGame returns userGame with correct fields`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = true)
        assertEquals(1, userGame.userId)
        assertEquals(1, userGame.gameId)
        assertEquals(true, userGame.synchronize)
    }

    @Test
    fun `createUserGame assigns sequential ids`() {
        val ug1 = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        val ug2 = repo.createUserGame(userId = 1, gameId = 2, synchronize = false)
        val ug3 = repo.createUserGame(userId = 2, gameId = 1, synchronize = false)
        assertEquals(1, ug1.id)
        assertEquals(2, ug2.id)
        assertEquals(3, ug3.id)
    }

    @Test
    fun `createUserGame persists to findAll`() {
        val ug1 = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        val ug2 = repo.createUserGame(userId = 2, gameId = 2, synchronize = true)
        assertEquals(listOf(ug1, ug2), repo.findAll())
    }

    @Test
    fun `findById returns correct userGame`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        assertEquals(userGame, repo.findById(userGame.id))
    }

    @Test
    fun `findById returns null when not found`() {
        assertNull(repo.findById(999))
    }

    @Test
    fun `findById returns null on empty repo`() {
        assertNull(repo.findById(1))
    }

    @Test
    fun `findByUserId returns all games for a user`() {
        val ug1 = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        val ug2 = repo.createUserGame(userId = 1, gameId = 2, synchronize = true)
        repo.createUserGame(userId = 2, gameId = 1, synchronize = false)
        assertEquals(listOf(ug1, ug2), repo.findByUserId(1))
    }

    @Test
    fun `findByUserId returns empty when user has no games`() {
        assertEquals(emptyList(), repo.findByUserId(999))
    }

    @Test
    fun `findByUserId returns empty on empty repo`() {
        assertEquals(emptyList(), repo.findByUserId(1))
    }

    @Test
    fun `findByUserIdAndGameId returns correct entry`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        assertEquals(userGame, repo.findByUserIdAndGameId(userId = 1, gameId = 1))
    }

    @Test
    fun `findByUserIdAndGameId returns null when userId does not match`() {
        repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        assertNull(repo.findByUserIdAndGameId(userId = 999, gameId = 1))
    }

    @Test
    fun `findByUserIdAndGameId returns null when gameId does not match`() {
        repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        assertNull(repo.findByUserIdAndGameId(userId = 1, gameId = 999))
    }

    @Test
    fun `findByUserIdAndGameId returns null on empty repo`() {
        assertNull(repo.findByUserIdAndGameId(userId = 1, gameId = 1))
    }

    @Test
    fun `save updates synchronize flag`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        repo.save(userGame.copy(synchronize = true))
        assertEquals(true, repo.findById(userGame.id)?.synchronize)
    }

    @Test
    fun `save updates all fields`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        val updated = userGame.copy(userId = 2, gameId = 2, synchronize = true)
        repo.save(updated)
        assertEquals(updated, repo.findById(userGame.id))
    }

    @Test
    fun `save does not duplicate userGame`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        repo.save(userGame.copy(synchronize = true))
        assertEquals(1, repo.findAll().size)
    }

    @Test
    fun `deleteById removes userGame`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        repo.deleteById(userGame.id)
        assertNull(repo.findById(userGame.id))
    }

    @Test
    fun `deleteById only removes the correct userGame`() {
        val ug1 = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        val ug2 = repo.createUserGame(userId = 2, gameId = 2, synchronize = true)
        repo.deleteById(ug1.id)
        assertNull(repo.findById(ug1.id))
        assertEquals(ug2, repo.findById(ug2.id))
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        repo.deleteById(999)
        assertEquals(userGame, repo.findById(userGame.id))
    }

    @Test
    fun `clear removes all userGames`() {
        repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        repo.createUserGame(userId = 2, gameId = 2, synchronize = true)
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
    }

    @Test
    fun `clear on empty repo does nothing`() {
        assertEquals(emptyList(), repo.findAll())
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
    }
}