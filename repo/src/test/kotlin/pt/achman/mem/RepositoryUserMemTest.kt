package pt.achman.mem

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.achman.interfaces.RepositoryUser
import pt.achman.token.Token
import pt.achman.token.TokenValidationInfo
import pt.achman.user.PasswordValidationInfo
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RepositoryUserMemTest {
    private lateinit var repo: RepositoryUser

    @BeforeEach
    fun setup() {
        repo = RepositoryUserMem()
    }

    @Test
    fun `createUser returns user with correct fields`() {
        val user = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        assertEquals("Alice", user.name)
        assertEquals("alice@gmail.com", user.email)
        assertEquals(PasswordValidationInfo("hash"), user.passwordValidation)
    }

    @Test
    fun `createUser assigns sequential ids`() {
        val user1 = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        val user2 = repo.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"))
        // admin is seeded with id 1, so new users start at 2
        assertEquals(2, user1.id)
        assertEquals(3, user2.id)
    }

    @Test
    fun `createUser persists to findAll`() {
        val admin = repo.findById(1)
        val user1 = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        val user2 = repo.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"))
        assertEquals(listOf(admin, user1, user2), repo.findAll())
    }

    @Test
    fun `findById returns seeded admin`() {
        assertNotNull(repo.findById(1))
    }

    @Test
    fun `findById returns correct user`() {
        val user = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        assertEquals(user, repo.findById(user.id))
    }

    @Test
    fun `findById returns null when not found`() {
        assertNull(repo.findById(999))
    }

    @Test
    fun `findByEmail returns correct user`() {
        val user = repo.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"))
        assertEquals(user, repo.findByEmail("bob@gmail.com"))
    }

    @Test
    fun `findByEmail returns null when not found`() {
        assertNull(repo.findByEmail("notfound@gmail.com"))
    }

    @Test
    fun `findByEmail is case sensitive`() {
        repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        assertNull(repo.findByEmail("Alice@gmail.com"))
        assertNull(repo.findByEmail("ALICE@GMAIL.COM"))
    }

    @Test
    fun `findByEmail returns seeded admin`() {
        assertNotNull(repo.findByEmail("admin@gmail.com"))
    }

    @Test
    fun `findAll returns only seeded admin on fresh repo`() {
        val all = repo.findAll()
        assertEquals(1, all.size)
        assertEquals(1, all.first().id)
    }

    @Test
    fun `findAll returns all users`() {
        val admin = repo.findById(1)
        val user1 = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        val user2 = repo.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"))
        assertEquals(listOf(admin, user1, user2), repo.findAll())
    }

    @Test
    fun `save updates existing user`() {
        val user = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        val updated = user.copy(name = "AliceUpdated", email = "updated@gmail.com")
        repo.save(updated)
        assertEquals(updated, repo.findById(user.id))
    }

    @Test
    fun `save does not duplicate user`() {
        val user = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        repo.save(user.copy(name = "Updated"))
        // admin + alice = 2
        assertEquals(2, repo.findAll().size)
    }

    @Test
    fun `deleteById removes user`() {
        val user = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        repo.deleteById(user.id)
        assertNull(repo.findById(user.id))
    }

    @Test
    fun `deleteById also removes user tokens`() {
        val user = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        val info = TokenValidationInfo("tokenAlice")
        repo.createToken(Token(info, user.id, Instant.now(), Instant.now()), maxTokens = 2)
        repo.deleteById(user.id)
        assertNull(repo.getTokenByTokenValidationInfo(info))
    }

    @Test
    fun `deleteById only removes the correct user`() {
        val user1 = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        val user2 = repo.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"))
        repo.deleteById(user1.id)
        assertNull(repo.findById(user1.id))
        assertEquals(user2, repo.findById(user2.id))
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        val user = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        repo.deleteById(999)
        assertEquals(user, repo.findById(user.id))
    }

    @Test
    fun `createToken and getTokenByTokenValidationInfo`() {
        val user = repo.createUser("Carol", "carol@gmail.com", PasswordValidationInfo("hash3"))
        val info = TokenValidationInfo("token123")
        val now = Instant.now()
        val token = Token(info, user.id, now, now)
        repo.createToken(token, maxTokens = 2)
        val result = repo.getTokenByTokenValidationInfo(info)
        assertNotNull(result)
        assertEquals(user, result.first)
        assertEquals(token, result.second)
    }

    @Test
    fun `createToken removes oldest when maxTokens exceeded`() {
        val user = repo.createUser("Dave", "dave@gmail.com", PasswordValidationInfo("hash4"))
        val init = Instant.now().minusSeconds(60)
        val t1 = Token(TokenValidationInfo("t1"), user.id, init, Instant.now().minusSeconds(10))
        val t2 = Token(TokenValidationInfo("t2"), user.id, init, Instant.now().minusSeconds(5))
        val t3 = Token(TokenValidationInfo("t3"), user.id, init, Instant.now())
        repo.createToken(t1, maxTokens = 2)
        repo.createToken(t2, maxTokens = 2)
        repo.createToken(t3, maxTokens = 2)
        assertNull(repo.getTokenByTokenValidationInfo(TokenValidationInfo("t1")))
        assertNotNull(repo.getTokenByTokenValidationInfo(TokenValidationInfo("t2")))
        assertNotNull(repo.getTokenByTokenValidationInfo(TokenValidationInfo("t3")))
    }

    @Test
    fun `createToken does not remove tokens of other users`() {
        val user1 = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        val user2 = repo.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"))
        val init = Instant.now().minusSeconds(60)
        val t1 = Token(TokenValidationInfo("t1"), user1.id, init, Instant.now().minusSeconds(10))
        val t2 = Token(TokenValidationInfo("t2"), user1.id, init, Instant.now().minusSeconds(5))
        val t3 = Token(TokenValidationInfo("t3"), user2.id, init, Instant.now())
        repo.createToken(t1, maxTokens = 1)
        repo.createToken(t2, maxTokens = 1)
        repo.createToken(t3, maxTokens = 1)
        // t1 removed because user1 exceeded maxTokens, t3 unaffected
        assertNull(repo.getTokenByTokenValidationInfo(TokenValidationInfo("t1")))
        assertNotNull(repo.getTokenByTokenValidationInfo(TokenValidationInfo("t2")))
        assertNotNull(repo.getTokenByTokenValidationInfo(TokenValidationInfo("t3")))
    }

    @Test
    fun `updateTokenLastUsed replaces token`() {
        val user = repo.createUser("Eve", "eve@gmail.com", PasswordValidationInfo("hash5"))
        val info = TokenValidationInfo("tokenEve")
        val init = Instant.now().minusSeconds(200)
        val oldToken = Token(info, user.id, init, Instant.now().minusSeconds(100))
        repo.createToken(oldToken, maxTokens = 2)
        val newToken = Token(info, user.id, init, Instant.now())
        repo.updateTokenLastUsed(newToken, newToken.lastUsedAt)
        val result = repo.getTokenByTokenValidationInfo(info)
        assertNotNull(result)
        assertEquals(newToken, result.second)
    }

    @Test
    fun `updateTokenLastUsed does not create a duplicate token`() {
        val user = repo.createUser("Eve", "eve@gmail.com", PasswordValidationInfo("hash5"))
        val info = TokenValidationInfo("tokenEve")
        val now = Instant.now()
        val token = Token(info, user.id, now, now)
        repo.createToken(token, maxTokens = 2)
        repo.updateTokenLastUsed(token.copy(lastUsedAt = now.plusSeconds(100)), now.plusSeconds(100))
        // still only one token with this validation info
        var count = 0
        repeat(100) {
            if (repo.getTokenByTokenValidationInfo(info) != null) count++
        }
        assertEquals(100, count)
    }

    @Test
    fun `removeTokenByValidationInfo removes token and returns count`() {
        val user = repo.createUser("Frank", "frank@gmail.com", PasswordValidationInfo("hash6"))
        val info = TokenValidationInfo("tokenFrank")
        repo.createToken(Token(info, user.id, Instant.now(), Instant.now()), maxTokens = 2)
        val removed = repo.removeTokenByValidationInfo(info)
        assertEquals(1, removed)
        assertNull(repo.getTokenByTokenValidationInfo(info))
    }

    @Test
    fun `removeTokenByValidationInfo returns 0 when token not found`() {
        assertEquals(0, repo.removeTokenByValidationInfo(TokenValidationInfo("nonexistent")))
    }

    @Test
    fun `removeTokenByValidationInfo only removes the correct token`() {
        val user = repo.createUser("Frank", "frank@gmail.com", PasswordValidationInfo("hash6"))
        val info1 = TokenValidationInfo("token1")
        val info2 = TokenValidationInfo("token2")
        val now = Instant.now()
        repo.createToken(Token(info1, user.id, now, now), maxTokens = 5)
        repo.createToken(Token(info2, user.id, now, now), maxTokens = 5)
        repo.removeTokenByValidationInfo(info1)
        assertNull(repo.getTokenByTokenValidationInfo(info1))
        assertNotNull(repo.getTokenByTokenValidationInfo(info2))
    }

    @Test
    fun `clear removes all users and tokens`() {
        val user = repo.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
        val info = TokenValidationInfo("tokenAlice")
        repo.createToken(Token(info, user.id, Instant.now(), Instant.now()), maxTokens = 2)
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
        assertNull(repo.getTokenByTokenValidationInfo(info))
    }

    @Test
    fun `clear on empty repo does nothing`() {
        repo.clear()
        assertEquals(emptyList(), repo.findAll())
    }
}