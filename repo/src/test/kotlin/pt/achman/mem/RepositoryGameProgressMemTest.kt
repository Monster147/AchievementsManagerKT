package pt.achman.mem

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.achman.interfaces.RepositoryGameProgress
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryGameProgressMemTest {
    private lateinit var repo: RepositoryGameProgress

    @BeforeEach
    fun setup() {
        repo = RepositoryGameProgressMem()
    }

    @Test
    fun `createGameProgress returns progress with correct fields`() {
        val progress = repo.createGameProgress(userId = 1, gameId = 1)
        assertEquals(1, progress.userId)
        assertEquals(1, progress.gameId)
        assertTrue(progress.completedAchievements.isEmpty())
    }

    @Test
    fun `createGameProgress assigns sequential ids`() {
        val p1 = repo.createGameProgress(userId = 1, gameId = 1)
        val p2 = repo.createGameProgress(userId = 1, gameId = 2)
        val p3 = repo.createGameProgress(userId = 2, gameId = 1)
        assertEquals(1, p1.id)
        assertEquals(2, p2.id)
        assertEquals(3, p3.id)
    }

    @Test
    fun `createGameProgress persists to findAll`() {
        val p1 = repo.createGameProgress(userId = 1, gameId = 1)
        val p2 = repo.createGameProgress(userId = 2, gameId = 2)
        assertEquals(listOf(p1, p2), repo.findAll())
    }

    @Test
    fun `findById returns correct progress`() {
        val progress = repo.createGameProgress(userId = 1, gameId = 1)
        assertEquals(progress, repo.findById(progress.id))
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
    fun `findByUserIdAndGameId returns correct progress`() {
        val progress = repo.createGameProgress(userId = 1, gameId = 1)
        assertEquals(progress, repo.findByUserIdAndGameId(userId = 1, gameId = 1))
    }

    @Test
    fun `findByUserIdAndGameId returns null when userId does not match`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        assertNull(repo.findByUserIdAndGameId(userId = 999, gameId = 1))
    }

    @Test
    fun `findByUserIdAndGameId returns null when gameId does not match`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        assertNull(repo.findByUserIdAndGameId(userId = 1, gameId = 999))
    }

    @Test
    fun `findByUserIdAndGameId returns null on empty repo`() {
        assertNull(repo.findByUserIdAndGameId(userId = 1, gameId = 1))
    }

    @Test
    fun `findByUserId returns all progress for a user`() {
        val p1 = repo.createGameProgress(userId = 1, gameId = 1)
        val p2 = repo.createGameProgress(userId = 1, gameId = 2)
        repo.createGameProgress(userId = 2, gameId = 1)
        assertEquals(listOf(p1, p2), repo.findByUserId(1))
    }

    @Test
    fun `findByUserId returns empty when user has no progress`() {
        assertEquals(emptyList(), repo.findByUserId(999))
    }

    @Test
    fun `findByUserId returns empty on empty repo`() {
        assertEquals(emptyList(), repo.findByUserId(1))
    }

    @Test
    fun `addCompletedAchievement adds achievement to existing progress`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        val updated = repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        assertTrue(updated.completedAchievements.contains(10))
    }

    @Test
    fun `addCompletedAchievement creates progress if not exists`() {
        val updated = repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        assertNotNull(updated)
        assertTrue(updated.completedAchievements.contains(10))
    }

    @Test
    fun `addCompletedAchievement does not duplicate achievements`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        val updated = repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        assertEquals(1, updated.completedAchievements.count { it == 10 })
    }

    @Test
    fun `addCompletedAchievement can add multiple different achievements`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 20)
        val updated = repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 30)
        assertEquals(listOf(10, 20, 30), updated.completedAchievements)
    }

    @Test
    fun `addCompletedAchievement persists changes`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        val found = repo.findByUserIdAndGameId(userId = 1, gameId = 1)
        assertTrue(found!!.completedAchievements.contains(10))
    }

    @Test
    fun `addCompletedAchievement does not affect other users progress`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        repo.createGameProgress(userId = 2, gameId = 1)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        val user2Progress = repo.findByUserIdAndGameId(userId = 2, gameId = 1)
        assertTrue(user2Progress!!.completedAchievements.isEmpty())
    }

    @Test
    fun `removeCompletedAchievement removes achievement from progress`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        val updated = repo.removeCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        assertTrue(!updated.completedAchievements.contains(10))
    }

    @Test
    fun `removeCompletedAchievement does nothing if achievement not present`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        val updated = repo.removeCompletedAchievement(userId = 1, gameId = 1, achievementId = 999)
        assertTrue(updated.completedAchievements.isEmpty())
    }

    @Test
    fun `removeCompletedAchievement only removes the correct achievement`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 20)
        val updated = repo.removeCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        assertTrue(!updated.completedAchievements.contains(10))
        assertTrue(updated.completedAchievements.contains(20))
    }

    @Test
    fun `removeCompletedAchievement persists changes`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        repo.removeCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        val found = repo.findByUserIdAndGameId(userId = 1, gameId = 1)
        assertTrue(found!!.completedAchievements.isEmpty())
    }

    @Test
    fun `removeCompletedAchievement creates progress if not exists`() {
        val updated = repo.removeCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        assertNotNull(updated)
        assertTrue(updated.completedAchievements.isEmpty())
    }

    @Test
    fun `removeCompletedAchievement does not affect other users progress`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        repo.createGameProgress(userId = 2, gameId = 1)
        repo.addCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        repo.addCompletedAchievement(userId = 2, gameId = 1, achievementId = 10)
        repo.removeCompletedAchievement(userId = 1, gameId = 1, achievementId = 10)
        val user2Progress = repo.findByUserIdAndGameId(userId = 2, gameId = 1)
        assertTrue(user2Progress!!.completedAchievements.contains(10))
    }

    @Test
    fun `save updates existing progress`() {
        val progress = repo.createGameProgress(userId = 1, gameId = 1)
        val updated = progress.copy(completedAchievements = listOf(1, 2, 3))
        repo.save(updated)
        assertEquals(listOf(1, 2, 3), repo.findById(progress.id)?.completedAchievements)
    }

    @Test
    fun `save does not duplicate progress`() {
        val progress = repo.createGameProgress(userId = 1, gameId = 1)
        repo.save(progress.copy(completedAchievements = listOf(1)))
        assertEquals(1, repo.findAll().size)
    }

    @Test
    fun `deleteById removes progress`() {
        val progress = repo.createGameProgress(userId = 1, gameId = 1)
        repo.deleteById(progress.id)
        assertNull(repo.findById(progress.id))
    }

    @Test
    fun `deleteById only removes the correct progress`() {
        val p1 = repo.createGameProgress(userId = 1, gameId = 1)
        val p2 = repo.createGameProgress(userId = 2, gameId = 2)
        repo.deleteById(p1.id)
        assertNull(repo.findById(p1.id))
        assertEquals(p2, repo.findById(p2.id))
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        val progress = repo.createGameProgress(userId = 1, gameId = 1)
        repo.deleteById(999)
        assertEquals(progress, repo.findById(progress.id))
    }

    @Test
    fun `clear removes all progress`() {
        repo.createGameProgress(userId = 1, gameId = 1)
        repo.createGameProgress(userId = 2, gameId = 2)
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
    }

    @Test
    fun `clear on empty repo does nothing`() {
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
    }
}