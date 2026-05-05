package pt.achman.mem

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.achman.interfaces.RepositoryAchievements
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RepositoryAchievementsMemTest {
    private lateinit var repo: RepositoryAchievements

    @BeforeEach
    fun setup() {
        repo = RepositoryAchievementsMem()
    }

    @Test
    fun `createAchievement returns achievement with correct fields`() {
        val achievement = repo.createAchievement("254397", "Did We Just Become Best Friends?", "https://icon.png", "Meet Clank", 1)
        assertEquals("254397", achievement.apiName)
        assertEquals("Did We Just Become Best Friends?", achievement.name)
        assertEquals("https://icon.png", achievement.icon)
        assertEquals("Meet Clank", achievement.description)
        assertEquals(1, achievement.gameId)
    }

    @Test
    fun `createAchievement assigns sequential ids`() {
        val a1 = repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        val a2 = repo.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", 1)
        val a3 = repo.createAchievement("api3", "Achievement 3", "icon3.png", "Desc 3", 1)
        assertEquals(1, a1.id)
        assertEquals(2, a2.id)
        assertEquals(3, a3.id)
    }

    @Test
    fun `createAchievement persists to findAll`() {
        val a1 = repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        val a2 = repo.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", 2)
        assertEquals(listOf(a1, a2), repo.findAll())
    }

    @Test
    fun `findById returns correct achievement`() {
        val achievement = repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        val found = repo.findById(achievement.id)
        assertNotNull(found)
        assertEquals(achievement, found)
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
    fun `findByGameId returns all achievements for a game`() {
        val a1 = repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        val a2 = repo.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", 1)
        val a3 = repo.createAchievement("api3", "Achievement 3", "icon3.png", "Desc 3", 2)
        assertEquals(listOf(a1, a2), repo.findByGameId(1))
        assertEquals(listOf(a3), repo.findByGameId(2))
    }

    @Test
    fun `findByGameId returns empty when no achievements for game`() {
        assertEquals(emptyList(), repo.findByGameId(999))
    }

    @Test
    fun `findByGameId returns empty on empty repo`() {
        assertEquals(emptyList(), repo.findByGameId(1))
    }

    @Test
    fun `findByApiName returns correct achievement`() {
        val achievement = repo.createAchievement("254397", "Did We Just Become Best Friends?", "icon.png", "Desc", 1)
        assertEquals(achievement, repo.findByApiName("254397"))
    }

    @Test
    fun `findByApiName returns null when not found`() {
        assertNull(repo.findByApiName("notexisting"))
    }

    @Test
    fun `findByApiName returns null on empty repo`() {
        assertNull(repo.findByApiName("api1"))
    }

    @Test
    fun `findByApiName only returns exact match`() {
        repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        assertNull(repo.findByApiName("api"))
        assertNull(repo.findByApiName("api11"))
    }

    @Test
    fun `removeAchievements removes all achievements for a game`() {
        repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        repo.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", 1)
        repo.createAchievement("api3", "Achievement 3", "icon3.png", "Desc 3", 2)
        repo.removeAchievements(1)
        assertEquals(emptyList(), repo.findByGameId(1))
        assertEquals(listOf(repo.findById(3)), repo.findByGameId(2))
    }

    @Test
    fun `removeAchievements on game with no achievements does nothing`() {
        repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        assertEquals(emptyList(), repo.findByGameId(999))
        repo.removeAchievements(999)
        assertEquals(emptyList(), repo.findByGameId(999))
        assertEquals(listOf(repo.findById(1)), repo.findByGameId(1))
    }

    @Test
    fun `findAll returns empty on empty repo`() {
        assertEquals(emptyList(), repo.findAll())
    }

    @Test
    fun `findAll returns all achievements`() {
        val a1 = repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        val a2 = repo.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", 2)
        val a3 = repo.createAchievement("api3", "Achievement 3", "icon3.png", "Desc 3", 3)
        assertEquals(listOf(a1, a2, a3), repo.findAll())
    }

    @Test
    fun `save updates existing achievement`() {
        val achievement = repo.createAchievement("api1", "Old Name", "icon.png", "Desc", 1)
        val updated = achievement.copy(name = "New Name")
        repo.save(updated)
        assertEquals("New Name", repo.findById(achievement.id)?.name)
    }

    @Test
    fun `save updates all fields`() {
        val achievement = repo.createAchievement("api1", "Old Name", "old_icon.png", "Old Desc", 1)
        val updated = achievement.copy(name = "New Name", icon = "new_icon.png", description = "New Desc", gameId = 2)
        repo.save(updated)
        val found = repo.findById(achievement.id)
        assertEquals(updated, found)
    }

    @Test
    fun `save does not duplicate achievement`() {
        val achievement = repo.createAchievement("api1", "Achievement", "icon.png", "Desc", 1)
        repo.save(achievement.copy(name = "Updated"))
        assertEquals(1, repo.findAll().size)
    }

    @Test
    fun `deleteById removes achievement`() {
        val achievement = repo.createAchievement("api1", "Achievement", "icon.png", "Desc", 1)
        repo.deleteById(achievement.id)
        assertNull(repo.findById(achievement.id))
    }

    @Test
    fun `deleteById only removes the correct achievement`() {
        val a1 = repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        val a2 = repo.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", 2)
        repo.deleteById(a1.id)
        assertNull(repo.findById(a1.id))
        assertEquals(a2, repo.findById(a2.id))
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        val achievement = repo.createAchievement("api1", "Achievement", "icon.png", "Desc", 1)
        repo.deleteById(999)
        assertEquals(achievement, repo.findById(achievement.id))
    }

    @Test
    fun `clear removes all achievements`() {
        repo.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", 1)
        repo.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", 2)
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
    }

    @Test
    fun `clear on empty repo does nothing`() {
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
    }
}
