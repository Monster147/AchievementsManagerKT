package pt.jsal.achman.jdbi

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.user.PasswordValidationInfo
import pt.jsal.achman.user.UserRole
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryUserGamesJdbiTest {
    companion object {
        private val jdbi: Jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL(Environment.getDbUrl())
                },
            ).configureWithAppRequirements()

        private val trxManager = TransactionManagerJdbi(jdbi)
    }

    @BeforeEach
    fun setup() {
        trxManager.run {
            repoUserGames.clear()
            repoGames.clear()
            repoUsers.clear()
        }
    }

    @Test
    fun `createUserGame returns userGame with correct fields`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val userGame = repoUserGames.createUserGame(user.id, game.id, true)
            assertEquals(user.id, userGame.userId)
            assertEquals(game.id, userGame.gameId)
            assertEquals(true, userGame.synchronize)
        }
    }

    @Test
    fun `createUserGame persists to findById`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val userGame = repoUserGames.createUserGame(user.id, game.id, false)
            val found = repoUserGames.findById(userGame.id)
            assertNotNull(found)
            assertEquals(userGame.id, found.id)
            assertEquals(userGame.userId, found.userId)
            assertEquals(userGame.gameId, found.gameId)
            assertEquals(userGame.synchronize, found.synchronize)
        }
    }

    @Test
    fun `createUserGame persists to findAll`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM, "cover.png")
            repoUserGames.createUserGame(user.id, g1.id, false)
            repoUserGames.createUserGame(user.id, g2.id, true)
            assertEquals(2, repoUserGames.findAll().size)
        }
    }

    @Test
    fun `findById returns null when not found`() {
        trxManager.run {
            assertNull(repoUserGames.findById(999))
        }
    }

    @Test
    fun `findById returns null on empty repo`() {
        trxManager.run {
            assertNull(repoUserGames.findById(1))
        }
    }

    @Test
    fun `findByUserId returns all games for a user`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"), UserRole.NORMAL)
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM, "cover.png")
            val ug1 = repoUserGames.createUserGame(u1.id, g1.id, false)
            val ug2 = repoUserGames.createUserGame(u1.id, g2.id, true)
            repoUserGames.createUserGame(u2.id, g1.id, false)
            val found = repoUserGames.findByUserId(u1.id)
            assertEquals(2, found.size)
            assertTrue(found.any { it.id == ug1.id })
            assertTrue(found.any { it.id == ug2.id })
        }
    }

    @Test
    fun `findByUserId returns empty when user has no games`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            assertEquals(emptyList(), repoUserGames.findByUserId(user.id))
        }
    }

    @Test
    fun `findByUserIdAndGameId returns correct entry`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val userGame = repoUserGames.createUserGame(user.id, game.id, false)
            val found = repoUserGames.findByUserIdAndGameId(user.id, game.id)
            assertNotNull(found)
            assertEquals(userGame.id, found.id)
        }
    }

    @Test
    fun `findByUserIdAndGameId returns null when userId does not match`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            repoUserGames.createUserGame(user.id, game.id, false)
            assertNull(repoUserGames.findByUserIdAndGameId(999, game.id))
        }
    }

    @Test
    fun `findByUserIdAndGameId returns null when gameId does not match`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            repoUserGames.createUserGame(user.id, game.id, false)
            assertNull(repoUserGames.findByUserIdAndGameId(user.id, 999))
        }
    }

    @Test
    fun `alterSyncOption toggles synchronize from false to true`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")

            val userGame = repoUserGames.createUserGame(user.id, game.id, false)

            val updated = repoUserGames.alterSyncOption(userGame)

            assertEquals(true, updated.synchronize)
        }
    }

    @Test
    fun `alterSyncOption toggles synchronize from true to false`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")

            val userGame = repoUserGames.createUserGame(user.id, game.id, true)

            val updated = repoUserGames.alterSyncOption(userGame)

            assertEquals(false, updated.synchronize)
        }
    }

    @Test
    fun `alterSyncOption persists change`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")

            val userGame = repoUserGames.createUserGame(user.id, game.id, false)

            repoUserGames.alterSyncOption(userGame)

            val found = repoUserGames.findById(userGame.id)
            assertEquals(true, found?.synchronize)
        }
    }

    @Test
    fun `alterSyncOption does not duplicate userGame`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")

            val userGame = repoUserGames.createUserGame(user.id, game.id, false)

            repoUserGames.alterSyncOption(userGame)

            assertEquals(1, repoUserGames.findAll().size)
        }
    }

    @Test
    fun `alterSyncOption only affects the correct userGame`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM, "cover.png")

            val ug1 = repoUserGames.createUserGame(user.id, g1.id, false)
            val ug2 = repoUserGames.createUserGame(user.id, g2.id, false)

            repoUserGames.alterSyncOption(ug1)

            val updated1 = repoUserGames.findById(ug1.id)
            val updated2 = repoUserGames.findById(ug2.id)

            assertEquals(true, updated1?.synchronize)
            assertEquals(false, updated2?.synchronize)
        }
    }

    @Test
    fun `alterSyncOption toggling twice returns to original value`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")

            val userGame = repoUserGames.createUserGame(user.id, game.id, false)

            val updated1 = repoUserGames.alterSyncOption(userGame)
            val updated2 = repoUserGames.alterSyncOption(updated1)

            assertEquals(false, updated2.synchronize)
        }
    }

    @Test
    fun `removeUserGames removes all games for a user`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"), UserRole.NORMAL)

            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM, "cover.png")

            repoUserGames.createUserGame(u1.id, g1.id, false)
            repoUserGames.createUserGame(u1.id, g2.id, true)
            repoUserGames.createUserGame(u2.id, g1.id, false)

            repoUserGames.removeUserGames(u1.id)

            val remainingU1 = repoUserGames.findByUserId(u1.id)
            val remainingU2 = repoUserGames.findByUserId(u2.id)

            assertTrue(remainingU1.isEmpty())
            assertEquals(1, remainingU2.size)
        }
    }

    @Test
    fun `removeUserGames does not affect other users`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"), UserRole.NORMAL)

            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")

            repoUserGames.createUserGame(u1.id, game.id, false)
            val ug2 = repoUserGames.createUserGame(u2.id, game.id, true)

            repoUserGames.removeUserGames(u1.id)

            assertNotNull(repoUserGames.findById(ug2.id))
        }
    }

    @Test
    fun `removeUserGames on user with no games does nothing`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)

            repoUserGames.removeUserGames(user.id)

            assertEquals(emptyList(), repoUserGames.findByUserId(user.id))
        }
    }

    @Test
    fun `removeUserGames on empty repo does nothing`() {
        trxManager.run {
            repoUserGames.removeUserGames(1)
            assertEquals(emptyList(), repoUserGames.findAll())
        }
    }

    @Test
    fun `save updates synchronize flag`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val userGame = repoUserGames.createUserGame(user.id, game.id, false)
            repoUserGames.save(userGame.copy(synchronize = true))
            assertEquals(true, repoUserGames.findById(userGame.id)?.synchronize)
        }
    }

    @Test
    fun `save does not duplicate userGame`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val userGame = repoUserGames.createUserGame(user.id, game.id, false)
            repoUserGames.save(userGame.copy(synchronize = true))
            assertEquals(1, repoUserGames.findAll().size)
        }
    }

    @Test
    fun `deleteById removes userGame`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val userGame = repoUserGames.createUserGame(user.id, game.id, false)
            repoUserGames.deleteById(userGame.id)
            assertNull(repoUserGames.findById(userGame.id))
        }
    }

    @Test
    fun `deleteById only removes the correct userGame`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM, "cover.png")
            val ug1 = repoUserGames.createUserGame(user.id, g1.id, false)
            val ug2 = repoUserGames.createUserGame(user.id, g2.id, true)
            repoUserGames.deleteById(ug1.id)
            assertNull(repoUserGames.findById(ug1.id))
            assertNotNull(repoUserGames.findById(ug2.id))
        }
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val userGame = repoUserGames.createUserGame(user.id, game.id, false)
            repoUserGames.deleteById(999)
            assertNotNull(repoUserGames.findById(userGame.id))
        }
    }

    @Test
    fun `deleting a user cascades to their userGames`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val userGame = repoUserGames.createUserGame(user.id, game.id, false)
            repoUsers.deleteById(user.id)
            assertNull(repoUserGames.findById(userGame.id))
        }
    }

    @Test
    fun `deleting a game cascades to userGames`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val userGame = repoUserGames.createUserGame(user.id, game.id, false)
            repoGames.deleteById(game.id)
            assertNull(repoUserGames.findById(userGame.id))
        }
    }

    @Test
    fun `clear removes all userGames`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.NORMAL)
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS, "cover.png")
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM, "cover.png")
            repoUserGames.createUserGame(user.id, g1.id, false)
            repoUserGames.createUserGame(user.id, g2.id, true)
            repoUserGames.clear()
            assertEquals(emptyList(), repoUserGames.findAll())
        }
    }

    @Test
    fun `clear on empty repo does nothing`() {
        trxManager.run {
            repoUserGames.clear()
            assertEquals(emptyList(), repoUserGames.findAll())
        }
    }
}
