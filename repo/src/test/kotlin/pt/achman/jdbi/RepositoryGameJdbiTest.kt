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

class RepositoryGameJdbiTest {
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
            repoGames.clear()
        }
    }

    @Test
    fun `createGame returns game with correct fields`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            assertEquals("3070", game.externalGameId)
            assertEquals("Ratchet & Clank", game.name)
            assertEquals(GameSource.RETROACHIEVEMENTS, game.source)
        }
    }

    @Test
    fun `createGame persists to findById`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val found = repoGames.findById(game.id)
            assertNotNull(found)
            assertEquals(game.id, found.id)
            assertEquals(game.externalGameId, found.externalGameId)
            assertEquals(game.name, found.name)
            assertEquals(game.source, found.source)
        }
    }

    @Test
    fun `createGame persists to findAll`() {
        trxManager.run {
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM)
            val all = repoGames.findAll()
            assertEquals(2, all.size)
            assertTrue(all.any { it.id == g1.id })
            assertTrue(all.any { it.id == g2.id })
        }
    }

    @Test
    fun `findById returns null when not found`() {
        trxManager.run {
            assertNull(repoGames.findById(999))
        }
    }

    @Test
    fun `findById returns null on empty repo`() {
        trxManager.run {
            assertNull(repoGames.findById(1))
        }
    }

    @Test
    fun `findByExternalId returns correct game`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val found = repoGames.findByExternalId("3070", GameSource.RETROACHIEVEMENTS)
            assertNotNull(found)
            assertEquals(game.id, found.id)
        }
    }

    @Test
    fun `findByExternalId distinguishes between sources`() {
        trxManager.run {
            val raGame = repoGames.createGame("100", "RA Game", GameSource.RETROACHIEVEMENTS)
            val steamGame = repoGames.createGame("100", "Steam Game", GameSource.STEAM)
            val foundRa = repoGames.findByExternalId("100", GameSource.RETROACHIEVEMENTS)
            val foundSteam = repoGames.findByExternalId("100", GameSource.STEAM)
            assertEquals(raGame.id, foundRa?.id)
            assertEquals(steamGame.id, foundSteam?.id)
        }
    }

    @Test
    fun `findByExternalId returns null when source does not match`() {
        trxManager.run {
            repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            assertNull(repoGames.findByExternalId("3070", GameSource.STEAM))
        }
    }

    @Test
    fun `findByExternalId returns null when not found`() {
        trxManager.run {
            assertNull(repoGames.findByExternalId("999", GameSource.STEAM))
        }
    }

    @Test
    fun `findAll returns empty on empty repo`() {
        trxManager.run {
            assertEquals(emptyList(), repoGames.findAll())
        }
    }

    @Test
    fun `save updates name`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Old Name", GameSource.RETROACHIEVEMENTS)
            val updated = game.copy(name = "New Name")
            repoGames.save(updated)
            assertEquals("New Name", repoGames.findById(game.id)?.name)
        }
    }

    @Test
    fun `save updates genre`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val updated = game.copy(genre = listOf(GameGenre.ACTION, GameGenre.ADVENTURE))
            repoGames.save(updated)
            val found = repoGames.findById(game.id)
            assertEquals(listOf(GameGenre.ACTION, GameGenre.ADVENTURE), found?.genre)
        }
    }

    @Test
    fun `save updates platform`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val updated = game.copy(platform = GamePlatform.PS2)
            repoGames.save(updated)
            assertEquals(GamePlatform.PS2, repoGames.findById(game.id)?.platform)
        }
    }

    @Test
    fun `save updates all fields`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Old Name", GameSource.RETROACHIEVEMENTS)
            val updated =
                game.copy(
                    name = "New Name",
                    genre = listOf(GameGenre.ACTION),
                    platform = GamePlatform.PS2,
                    releaseYear = "2002",
                    cover = "/covers/ratchet.jpg",
                )
            repoGames.save(updated)
            val found = repoGames.findById(game.id)
            assertNotNull(found)
            assertEquals("New Name", found.name)
            assertEquals(listOf(GameGenre.ACTION), found.genre)
            assertEquals(GamePlatform.PS2, found.platform)
            assertEquals("2002", found.releaseYear)
            assertEquals("/covers/ratchet.jpg", found.cover)
        }
    }

    @Test
    fun `save does not duplicate game`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            repoGames.save(game.copy(name = "Updated"))
            assertEquals(1, repoGames.findAll().size)
        }
    }

    @Test
    fun `deleteById removes game`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            repoGames.deleteById(game.id)
            assertNull(repoGames.findById(game.id))
        }
    }

    @Test
    fun `deleteById only removes the correct game`() {
        trxManager.run {
            val g1 = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            val g2 = repoGames.createGame("730", "CS2", GameSource.STEAM)
            repoGames.deleteById(g1.id)
            assertNull(repoGames.findById(g1.id))
            assertNotNull(repoGames.findById(g2.id))
        }
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        trxManager.run {
            val game = repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            repoGames.deleteById(999)
            assertNotNull(repoGames.findById(game.id))
        }
    }

    @Test
    fun `clear removes all games`() {
        trxManager.run {
            repoGames.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
            repoGames.createGame("730", "CS2", GameSource.STEAM)
            repoGames.clear()
            assertEquals(emptyList(), repoGames.findAll())
        }
    }

    @Test
    fun `clear on empty repo does nothing`() {
        trxManager.run {
            repoGames.clear()
            assertEquals(emptyList(), repoGames.findAll())
        }
    }
}
