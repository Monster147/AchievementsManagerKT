package pt.achman.jdbi

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import pt.achman.game.GameGenre
import pt.achman.game.GamePlatform
import pt.achman.game.GameSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryAchievementsJdbiTest {
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
            repoAchievements.clear()
            repoGames.clear()
        }
    }

    @Test
    fun `createAchievement returns achievement with correct fields`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement =
                repoAchievements.createAchievement(
                    "254397",
                    "Did We Just Become Best Friends?",
                    "https://icon.png",
                    "Meet Clank",
                    game.id,
                )
            assertEquals("254397", achievement.apiName)
            assertEquals("Did We Just Become Best Friends?", achievement.name)
            assertEquals("https://icon.png", achievement.icon)
            assertEquals("Meet Clank", achievement.description)
            assertEquals(game.id, achievement.gameId)
        }
    }

    @Test
    fun `createAchievement persists to findById`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement =
                repoAchievements.createAchievement(
                    "254397",
                    "Did We Just Become Best Friends?",
                    "https://icon.png",
                    "Meet Clank",
                    game.id,
                )
            val found = repoAchievements.findById(achievement.id)
            assertNotNull(found)
            assertEquals(achievement.id, found.id)
            assertEquals(achievement.apiName, found.apiName)
            assertEquals(achievement.name, found.name)
        }
    }

    @Test
    fun `createAchievement persists to findAll`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val a1 = repoAchievements.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", game.id)
            val a2 = repoAchievements.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", game.id)
            val all = repoAchievements.findAll()
            assertEquals(2, all.size)
            assertTrue(all.any { it.id == a1.id })
            assertTrue(all.any { it.id == a2.id })
        }
    }

    @Test
    fun `findById returns null when not found`() {
        trxManager.run {
            assertNull(repoAchievements.findById(999))
        }
    }

    @Test
    fun `findById returns null on empty repo`() {
        trxManager.run {
            assertNull(repoAchievements.findById(1))
        }
    }

    @Test
    fun `findByGameId returns all achievements for a game`() {
        trxManager.run {
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM)
            val a1 = repoAchievements.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", g1.id)
            val a2 = repoAchievements.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", g1.id)
            repoAchievements.createAchievement("api3", "Achievement 3", "icon3.png", "Desc 3", g2.id)
            val found = repoAchievements.findByGameId(g1.id)
            assertEquals(2, found.size)
            assertTrue(found.any { it.id == a1.id })
            assertTrue(found.any { it.id == a2.id })
        }
    }

    @Test
    fun `findByGameId returns empty when no achievements for game`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            assertEquals(emptyList(), repoAchievements.findByGameId(game.id))
        }
    }

    @Test
    fun `findByApiName returns correct achievement`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement =
                repoAchievements.createAchievement(
                    "254397",
                    "Did We Just Become Best Friends?",
                    "icon.png",
                    "Desc",
                    game.id,
                )
            val found = repoAchievements.findByApiName("254397")
            assertNotNull(found)
            assertEquals(achievement.id, found.id)
        }
    }

    @Test
    fun `findByApiName returns null when not found`() {
        trxManager.run {
            assertNull(repoAchievements.findByApiName("notexisting"))
        }
    }

    @Test
    fun `findByApiName only returns exact match`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            repoAchievements.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", game.id)
            assertNull(repoAchievements.findByApiName("api"))
            assertNull(repoAchievements.findByApiName("api11"))
        }
    }

    @Test
    fun `removeAchievements removes all achievements for a game`() {
        trxManager.run {
            val game1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val game2 = repoGames.createGame("3072", "Ratchet & Clank 2", GameSource.RETROACHIEVEMENTS)
            repoAchievements.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", game1.id)
            repoAchievements.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", game1.id)
            val achievement = repoAchievements.createAchievement("api3", "Achievement 3", "icon3.png", "Desc 3", game2.id)
            repoAchievements.removeAchievements(game1.id)
            assertTrue(repoAchievements.findByGameId(game1.id).isEmpty())
            assertEquals(listOf(repoAchievements.findById(achievement.id)), repoAchievements.findByGameId(game2.id))
        }
    }

    @Test
    fun `removeAchievements on game with no achievements does nothing`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", game.id)
            assertTrue(repoAchievements.findByGameId(999).isEmpty())
            repoAchievements.removeAchievements(999)
            assertTrue(repoAchievements.findByGameId(999).isEmpty())
            assertEquals(listOf(repoAchievements.findById(achievement.id)), repoAchievements.findByGameId(game.id))
        }
    }

    @Test
    fun `updateGameInfo updates all fields in database`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Old Name", GameSource.RETROACHIEVEMENTS)

            val updated =
                repoGames.updateGameInfo(
                    game = game,
                    externalGameId = "9999",
                    name = "New Name",
                    genres = listOf(GameGenre.ACTION),
                    platform = GamePlatform.PC,
                    releaseYear = "2024",
                    source = GameSource.STEAM,
                    cover = "cover.jpg",
                )

            val fromDb = repoGames.findById(game.id)

            assertEquals(updated, fromDb)
        }
    }

    @Test
    fun `updateGameInfo keeps old values when null is passed`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Name", GameSource.RETROACHIEVEMENTS)

            val updated =
                repoGames.updateGameInfo(
                    game = game,
                    externalGameId = null,
                    name = null,
                    genres = null,
                    platform = null,
                    releaseYear = null,
                    source = null,
                    cover = null,
                )

            val fromDb = repoGames.findById(game.id)

            assertEquals(updated, fromDb)
        }
    }

    @Test
    fun `updateGameInfo persists changes`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Old Name", GameSource.RETROACHIEVEMENTS)

            repoGames.updateGameInfo(
                game = game,
                externalGameId = null,
                name = "Updated Name",
                genres = null,
                platform = null,
                releaseYear = null,
                source = null,
                cover = null,
            )

            val fromDb = repoGames.findById(game.id)

            assertEquals("Updated Name", fromDb?.name)
        }
    }

    @Test
    fun `updateGameInfo does not create new row`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Name", GameSource.RETROACHIEVEMENTS)

            repoGames.updateGameInfo(
                game = game,
                externalGameId = "999",
                name = "Updated",
                genres = null,
                platform = null,
                releaseYear = null,
                source = null,
                cover = null,
            )

            val all = repoGames.findAll()
            assertEquals(1, all.size)
        }
    }

    @Test
    fun `updateGameInfo updates genres array correctly`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Name", GameSource.RETROACHIEVEMENTS)

            repoGames.updateGameInfo(
                game = game,
                externalGameId = null,
                name = null,
                genres = listOf(GameGenre.ACTION, GameGenre.ADVENTURE),
                platform = null,
                releaseYear = null,
                source = null,
                cover = null,
            )

            val fromDb = repoGames.findById(game.id)

            assertEquals(listOf(GameGenre.ACTION, GameGenre.ADVENTURE), fromDb?.genre)
        }
    }

    @Test
    fun `findAll returns empty on empty repo`() {
        trxManager.run {
            assertEquals(emptyList(), repoAchievements.findAll())
        }
    }

    @Test
    fun `save updates existing achievement`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Old Name", "icon.png", "Desc", game.id)
            repoAchievements.save(achievement.copy(name = "New Name"))
            assertEquals("New Name", repoAchievements.findById(achievement.id)?.name)
        }
    }

    @Test
    fun `save updates all fields`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Old Name", "old.png", "Old Desc", game.id)
            val updated = achievement.copy(name = "New Name", icon = "new.png", description = "New Desc")
            repoAchievements.save(updated)
            val found = repoAchievements.findById(achievement.id)
            assertEquals(updated.name, found?.name)
            assertEquals(updated.icon, found?.icon)
            assertEquals(updated.description, found?.description)
        }
    }

    @Test
    fun `save does not duplicate achievement`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoAchievements.save(achievement.copy(name = "Updated"))
            assertEquals(1, repoAchievements.findAll().size)
        }
    }

    @Test
    fun `deleteById removes achievement`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoAchievements.deleteById(achievement.id)
            assertNull(repoAchievements.findById(achievement.id))
        }
    }

    @Test
    fun `deleteById only removes the correct achievement`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val a1 = repoAchievements.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", game.id)
            val a2 = repoAchievements.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", game.id)
            repoAchievements.deleteById(a1.id)
            assertNull(repoAchievements.findById(a1.id))
            assertNotNull(repoAchievements.findById(a2.id))
        }
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoAchievements.deleteById(999)
            assertNotNull(repoAchievements.findById(achievement.id))
        }
    }

    @Test
    fun `clear removes all achievements`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            repoAchievements.createAchievement("api1", "Achievement 1", "icon1.png", "Desc 1", game.id)
            repoAchievements.createAchievement("api2", "Achievement 2", "icon2.png", "Desc 2", game.id)
            repoAchievements.clear()
            assertEquals(emptyList(), repoAchievements.findAll())
        }
    }

    @Test
    fun `clear on empty repo does nothing`() {
        trxManager.run {
            repoAchievements.clear()
            assertEquals(emptyList(), repoAchievements.findAll())
        }
    }

    @Test
    fun `deleting a game cascades to its achievements`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val achievement = repoAchievements.createAchievement("api1", "Achievement", "icon.png", "Desc", game.id)
            repoGames.deleteById(game.id)
            assertNull(repoAchievements.findById(achievement.id))
        }
    }
}
