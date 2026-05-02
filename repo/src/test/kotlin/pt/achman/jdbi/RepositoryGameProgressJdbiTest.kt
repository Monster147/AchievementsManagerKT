package pt.achman.jdbi

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import pt.achman.game.GameSource
import pt.achman.user.PasswordValidationInfo
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryGameProgressJdbiTest {
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
            repoGameProgress.clear()
            repoAchievements.clear()
            repoGames.clear()
            repoUsers.clear()
        }
    }

    @Test
    fun `createGameProgress returns progress with correct fields`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val progress = repoGameProgress.createGameProgress(user.id, game.id)
            assertEquals(user.id, progress.userId)
            assertEquals(game.id, progress.gameId)
            assertTrue(progress.completedAchievements.isEmpty())
        }
    }

    @Test
    fun `createGameProgress persists to findById`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val progress = repoGameProgress.createGameProgress(user.id, game.id)
            val found = repoGameProgress.findById(progress.id)
            assertNotNull(found)
            assertEquals(progress.id, found.id)
            assertEquals(progress.userId, found.userId)
            assertEquals(progress.gameId, found.gameId)
        }
    }

    @Test
    fun `findById returns null when not found`() {
        trxManager.run {
            assertNull(repoGameProgress.findById(999))
        }
    }

    @Test
    fun `findById returns null on empty repo`() {
        trxManager.run {
            assertNull(repoGameProgress.findById(1))
        }
    }

    @Test
    fun `findByUserIdAndGameId returns correct progress`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val progress = repoGameProgress.createGameProgress(user.id, game.id)
            val found = repoGameProgress.findByUserIdAndGameId(user.id, game.id)
            assertNotNull(found)
            assertEquals(progress.id, found.id)
        }
    }

    @Test
    fun `findByUserIdAndGameId returns null when userId does not match`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            repoGameProgress.createGameProgress(user.id, game.id)
            assertNull(repoGameProgress.findByUserIdAndGameId(999, game.id))
        }
    }

    @Test
    fun `findByUserIdAndGameId returns null when gameId does not match`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            repoGameProgress.createGameProgress(user.id, game.id)
            assertNull(repoGameProgress.findByUserIdAndGameId(user.id, 999))
        }
    }

    @Test
    fun `findByUserId returns all progress for a user`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"))
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM)
            val p1 = repoGameProgress.createGameProgress(u1.id, g1.id)
            val p2 = repoGameProgress.createGameProgress(u1.id, g2.id)
            repoGameProgress.createGameProgress(u2.id, g1.id)
            val found = repoGameProgress.findByUserId(u1.id)
            assertEquals(2, found.size)
            assertTrue(found.any { it.id == p1.id })
            assertTrue(found.any { it.id == p2.id })
        }
    }

    @Test
    fun `findByUserId returns empty when user has no progress`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            assertEquals(emptyList(), repoGameProgress.findByUserId(user.id))
        }
    }

    @Test
    fun `addCompletedAchievement adds achievement to progress`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoGameProgress.createGameProgress(user.id, game.id)
            val updated = repoGameProgress.addCompletedAchievement(user.id, game.id, achievement.id)
            assertTrue(updated.completedAchievements.contains(achievement.id))
        }
    }

    @Test
    fun `addCompletedAchievement persists changes`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoGameProgress.createGameProgress(user.id, game.id)
            repoGameProgress.addCompletedAchievement(user.id, game.id, achievement.id)
            val found = repoGameProgress.findByUserIdAndGameId(user.id, game.id)
            assertTrue(found!!.completedAchievements.contains(achievement.id))
        }
    }

    @Test
    fun `addCompletedAchievement does not duplicate achievements`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoGameProgress.createGameProgress(user.id, game.id)
            repoGameProgress.addCompletedAchievement(user.id, game.id, achievement.id)
            val updated = repoGameProgress.addCompletedAchievement(user.id, game.id, achievement.id)
            assertEquals(1, updated.completedAchievements.count { it == achievement.id })
        }
    }

    @Test
    fun `addCompletedAchievement creates progress if not exists`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            val updated = repoGameProgress.addCompletedAchievement(user.id, game.id, achievement.id)
            assertNotNull(updated)
            assertTrue(updated.completedAchievements.contains(achievement.id))
        }
    }

    @Test
    fun `addCompletedAchievement does not affect other users progress`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoGameProgress.createGameProgress(u1.id, game.id)
            repoGameProgress.createGameProgress(u2.id, game.id)
            repoGameProgress.addCompletedAchievement(u1.id, game.id, achievement.id)
            val u2Progress = repoGameProgress.findByUserIdAndGameId(u2.id, game.id)
            assertTrue(u2Progress!!.completedAchievements.isEmpty())
        }
    }

    @Test
    fun `addCompletedAchievement can add multiple achievements`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val a1 = repoAchievements.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", game.id)
            val a2 = repoAchievements.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", game.id)
            val a3 = repoAchievements.createAchievement("api3", "Achievement 3", "icon3.png", "Desc 3", game.id)
            repoGameProgress.createGameProgress(user.id, game.id)
            repoGameProgress.addCompletedAchievement(user.id, game.id, a1.id)
            repoGameProgress.addCompletedAchievement(user.id, game.id, a2.id)
            val updated = repoGameProgress.addCompletedAchievement(user.id, game.id, a3.id)
            assertEquals(3, updated.completedAchievements.size)
            assertTrue(updated.completedAchievements.containsAll(listOf(a1.id, a2.id, a3.id)))
        }
    }

    @Test
    fun `removeCompletedAchievement removes achievement from progress`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoGameProgress.createGameProgress(user.id, game.id)
            repoGameProgress.addCompletedAchievement(user.id, game.id, achievement.id)
            val updated = repoGameProgress.removeCompletedAchievement(user.id, game.id, achievement.id)
            assertTrue(!updated.completedAchievements.contains(achievement.id))
        }
    }

    @Test
    fun `removeCompletedAchievement persists changes`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoGameProgress.createGameProgress(user.id, game.id)
            repoGameProgress.addCompletedAchievement(user.id, game.id, achievement.id)
            repoGameProgress.removeCompletedAchievement(user.id, game.id, achievement.id)
            val found = repoGameProgress.findByUserIdAndGameId(user.id, game.id)
            assertTrue(found!!.completedAchievements.isEmpty())
        }
    }

    @Test
    fun `removeCompletedAchievement does nothing if achievement not present`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            repoGameProgress.createGameProgress(user.id, game.id)
            val updated = repoGameProgress.removeCompletedAchievement(user.id, game.id, 999)
            assertTrue(updated.completedAchievements.isEmpty())
        }
    }

    @Test
    fun `removeCompletedAchievement only removes correct achievement`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val a1 = repoAchievements.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", game.id)
            val a2 = repoAchievements.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", game.id)
            repoGameProgress.createGameProgress(user.id, game.id)
            repoGameProgress.addCompletedAchievement(user.id, game.id, a1.id)
            repoGameProgress.addCompletedAchievement(user.id, game.id, a2.id)
            val updated = repoGameProgress.removeCompletedAchievement(user.id, game.id, a1.id)
            assertTrue(!updated.completedAchievements.contains(a1.id))
            assertTrue(updated.completedAchievements.contains(a2.id))
        }
    }

    @Test
    fun `removeCompletedAchievement does not affect other users progress`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoGameProgress.createGameProgress(u1.id, game.id)
            repoGameProgress.createGameProgress(u2.id, game.id)
            repoGameProgress.addCompletedAchievement(u1.id, game.id, achievement.id)
            repoGameProgress.addCompletedAchievement(u2.id, game.id, achievement.id)
            repoGameProgress.removeCompletedAchievement(u1.id, game.id, achievement.id)
            val u2Progress = repoGameProgress.findByUserIdAndGameId(u2.id, game.id)
            assertTrue(u2Progress!!.completedAchievements.contains(achievement.id))
        }
    }

    @Test
    fun `deleteById removes progress`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val progress = repoGameProgress.createGameProgress(user.id, game.id)
            repoGameProgress.deleteById(progress.id)
            assertNull(repoGameProgress.findById(progress.id))
        }
    }

    @Test
    fun `deleteById only removes the correct progress`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM)
            val p1 = repoGameProgress.createGameProgress(user.id, g1.id)
            val p2 = repoGameProgress.createGameProgress(user.id, g2.id)
            repoGameProgress.deleteById(p1.id)
            assertNull(repoGameProgress.findById(p1.id))
            assertNotNull(repoGameProgress.findById(p2.id))
        }
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val progress = repoGameProgress.createGameProgress(user.id, game.id)
            repoGameProgress.deleteById(999)
            assertNotNull(repoGameProgress.findById(progress.id))
        }
    }

    @Test
    fun `deleting a user cascades to their game progress`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val progress = repoGameProgress.createGameProgress(user.id, game.id)
            repoUsers.deleteById(user.id)
            assertNull(repoGameProgress.findById(progress.id))
        }
    }

    @Test
    fun `deleting a game cascades to game progress`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val progress = repoGameProgress.createGameProgress(user.id, game.id)
            repoGames.deleteById(game.id)
            assertNull(repoGameProgress.findById(progress.id))
        }
    }

    @Test
    fun `clear removes all progress`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM)
            repoGameProgress.createGameProgress(user.id, g1.id)
            repoGameProgress.createGameProgress(user.id, g2.id)
            repoGameProgress.clear()
            assertEquals(emptyList(), repoGameProgress.findAll())
        }
    }

    @Test
    fun `clear on empty repo does nothing`() {
        trxManager.run {
            repoGameProgress.clear()
            assertEquals(emptyList(), repoGameProgress.findAll())
        }
    }
}
