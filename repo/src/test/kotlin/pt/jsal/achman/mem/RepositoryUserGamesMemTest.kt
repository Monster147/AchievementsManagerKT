package pt.jsal.achman.mem

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.jsal.achman.interfaces.RepositoryUserGames
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    fun `alterSyncOption toggles synchronize from false to true`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)

        val updated = repo.alterSyncOption(userGame)

        assertEquals(true, updated.synchronize)
    }

    @Test
    fun `alterSyncOption toggles synchronize from true to false`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = true)

        val updated = repo.alterSyncOption(userGame)

        assertEquals(false, updated.synchronize)
    }

    @Test
    fun `alterSyncOption persists change`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)

        repo.alterSyncOption(userGame)

        val found = repo.findById(userGame.id)
        assertEquals(true, found?.synchronize)
    }

    @Test
    fun `alterSyncOption does not duplicate userGame`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)

        repo.alterSyncOption(userGame)

        assertEquals(1, repo.findAll().size)
    }

    @Test
    fun `alterSyncOption only affects the correct userGame`() {
        val ug1 = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)
        val ug2 = repo.createUserGame(userId = 2, gameId = 2, synchronize = false)

        repo.alterSyncOption(ug1)

        val updated1 = repo.findById(ug1.id)
        val updated2 = repo.findById(ug2.id)

        assertEquals(true, updated1?.synchronize)
        assertEquals(false, updated2?.synchronize)
    }

    @Test
    fun `alterSyncOption toggling twice returns to original value`() {
        val userGame = repo.createUserGame(userId = 1, gameId = 1, synchronize = false)

        val updated1 = repo.alterSyncOption(userGame)
        val updated2 = repo.alterSyncOption(updated1)

        assertEquals(false, updated2.synchronize)
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
