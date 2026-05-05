package pt.achman.mem

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.achman.game.GamePlatform
import pt.achman.game.GameSource
import pt.achman.interfaces.RepositoryGame
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepositoryGameMemTest {
    private lateinit var repo: RepositoryGame

    @BeforeEach
    fun setup() {
        repo = RepositoryGameMem()
    }

    @Test
    fun `createGame returns game with correct fields`() {
        val game = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        assertEquals("3070", game.externalGameId)
        assertEquals("Ratchet & Clank", game.name)
        assertEquals(GameSource.RETROACHIEVEMENTS, game.source)
    }

    @Test
    fun `createGame assigns sequential ids`() {
        val g1 = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        val g2 = repo.createGame("730", "CS2", GameSource.STEAM)
        val g3 = repo.createGame("1", "Some PSN Game", GameSource.PSN)
        assertEquals(1, g1.id)
        assertEquals(2, g2.id)
        assertEquals(3, g3.id)
    }

    @Test
    fun `createGame persists to findAll`() {
        val g1 = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        val g2 = repo.createGame("730", "CS2", GameSource.STEAM)
        assertEquals(listOf(g1, g2), repo.findAll())
    }

    @Test
    fun `findById returns correct game`() {
        val game = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        assertEquals(game, repo.findById(game.id))
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
    fun `findByExternalId returns correct game`() {
        val game = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        assertEquals(game, repo.findByExternalId("3070", GameSource.RETROACHIEVEMENTS))
    }

    @Test
    fun `findByExternalId returns null when not found`() {
        assertNull(repo.findByExternalId("999", GameSource.STEAM))
    }

    @Test
    fun `findByExternalId distinguishes between sources`() {
        val raGame = repo.createGame("100", "RA Game", GameSource.RETROACHIEVEMENTS)
        val steamGame = repo.createGame("100", "Steam Game", GameSource.STEAM)
        assertEquals(raGame, repo.findByExternalId("100", GameSource.RETROACHIEVEMENTS))
        assertEquals(steamGame, repo.findByExternalId("100", GameSource.STEAM))
    }

    @Test
    fun `findByExternalId returns null when source does not match`() {
        repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        assertNull(repo.findByExternalId("3070", GameSource.STEAM))
    }

    @Test
    fun `findByExternalId returns null on empty repo`() {
        assertNull(repo.findByExternalId("3070", GameSource.RETROACHIEVEMENTS))
    }

    @Test
    fun `updateGameInfo updates all fields`() {
        val game = repo.createGame("3070", "Old Name", GameSource.RETROACHIEVEMENTS)

        val updated =
            repo.updateGameInfo(
                game = game,
                externalGameId = "9999",
                name = "New Name",
                genres = emptyList(),
                platform = GamePlatform.PC,
                releaseYear = "2024",
                source = GameSource.STEAM,
                cover = "cover.jpg",
            )

        assertEquals("9999", updated.externalGameId)
        assertEquals("New Name", updated.name)
        assertEquals(emptyList(), updated.genre)
        assertEquals(GamePlatform.PC, updated.platform)
        assertEquals("2024", updated.releaseYear)
        assertEquals(GameSource.STEAM, updated.source)
        assertEquals("cover.jpg", updated.cover)
    }

    @Test
    fun `updateGameInfo keeps old values when null is passed`() {
        val game = repo.createGame("3070", "Name", GameSource.RETROACHIEVEMENTS)

        val updated =
            repo.updateGameInfo(
                game = game,
                externalGameId = null,
                name = null,
                genres = null,
                platform = null,
                releaseYear = null,
                source = null,
                cover = null,
            )

        assertEquals(game, updated)
    }

    @Test
    fun `updateGameInfo persists changes in repository`() {
        val game = repo.createGame("3070", "Old Name", GameSource.RETROACHIEVEMENTS)

        repo.updateGameInfo(
            game = game,
            externalGameId = null,
            name = "Updated Name",
            genres = null,
            platform = null,
            releaseYear = null,
            source = null,
            cover = null,
        )

        val stored = repo.findById(game.id)
        assertEquals("Updated Name", stored?.name)
    }

    @Test
    fun `updateGameInfo does not duplicate game`() {
        val game = repo.createGame("3070", "Name", GameSource.RETROACHIEVEMENTS)

        repo.updateGameInfo(
            game = game,
            externalGameId = "999",
            name = "Updated",
            genres = null,
            platform = null,
            releaseYear = null,
            source = null,
            cover = null,
        )

        assertEquals(1, repo.findAll().size)
    }

    @Test
    fun `updateGameInfo updates only provided fields`() {
        val game = repo.createGame("3070", "Name", GameSource.RETROACHIEVEMENTS)

        val updated =
            repo.updateGameInfo(
                game = game,
                externalGameId = null,
                name = "Updated Name",
                genres = null,
                platform = null,
                releaseYear = null,
                source = null,
                cover = null,
            )

        assertEquals("Updated Name", updated.name)
        assertEquals(game.externalGameId, updated.externalGameId)
        assertEquals(game.source, updated.source)
    }

    @Test
    fun `findAll returns empty on empty repo`() {
        assertEquals(emptyList(), repo.findAll())
    }

    @Test
    fun `findAll returns all games`() {
        val g1 = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        val g2 = repo.createGame("730", "CS2", GameSource.STEAM)
        val g3 = repo.createGame("1", "Some PSN Game", GameSource.PSN)
        assertEquals(listOf(g1, g2, g3), repo.findAll())
    }

    @Test
    fun `save updates existing game`() {
        val game = repo.createGame("3070", "Old Name", GameSource.RETROACHIEVEMENTS)
        val updated = game.copy(name = "New Name")
        repo.save(updated)
        assertEquals("New Name", repo.findById(game.id)?.name)
    }

    @Test
    fun `save updates all fields`() {
        val game = repo.createGame("3070", "Old Name", GameSource.RETROACHIEVEMENTS)
        val updated = game.copy(name = "New Name", externalGameId = "9999", source = GameSource.STEAM)
        repo.save(updated)
        assertEquals(updated, repo.findById(game.id))
    }

    @Test
    fun `save does not duplicate game`() {
        val game = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        repo.save(game.copy(name = "Updated"))
        assertEquals(1, repo.findAll().size)
    }

    @Test
    fun `deleteById removes game`() {
        val game = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        repo.deleteById(game.id)
        assertNull(repo.findById(game.id))
    }

    @Test
    fun `deleteById only removes the correct game`() {
        val g1 = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        val g2 = repo.createGame("730", "CS2", GameSource.STEAM)
        repo.deleteById(g1.id)
        assertNull(repo.findById(g1.id))
        assertEquals(g2, repo.findById(g2.id))
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        val game = repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        repo.deleteById(999)
        assertEquals(game, repo.findById(game.id))
    }

    @Test
    fun `clear removes all games`() {
        repo.createGame("3070", "Ratchet & Clank", GameSource.RETROACHIEVEMENTS)
        repo.createGame("730", "CS2", GameSource.STEAM)
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
    }

    @Test
    fun `clear on empty repo does nothing`() {
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
    }
}
