package pt.jsal.achman.jdbi

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import pt.jsal.achman.token.Token
import pt.jsal.achman.token.TokenValidationInfo
import pt.jsal.achman.user.PasswordValidationInfo
import pt.jsal.achman.user.UserRole
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryUserJdbiTest {
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
            repoUsers.clear()
        }
    }

    @Test
    fun `createUser returns user with correct fields`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            assertEquals("Alice", user.name)
            assertEquals("alice@gmail.com", user.email)
            assertEquals(PasswordValidationInfo("hash"), user.passwordValidation)
        }
    }

    @Test
    fun `createUser with ADMIN role`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.ADMIN)
            assertEquals(UserRole.ADMIN, user.role)
        }
    }

    @Test
    fun `createUser persists to findById`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val found = repoUsers.findById(user.id)
            assertNotNull(found)
            assertEquals(user.id, found.id)
            assertEquals(user.name, found.name)
            assertEquals(user.email, found.email)
        }
    }

    @Test
    fun `createUser persists to findAll`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"), UserRole.USER)
            val all = repoUsers.findAll()
            assertEquals(2, all.size)
            assertTrue(all.any { it.id == u1.id })
            assertTrue(all.any { it.id == u2.id })
        }
    }

    @Test
    fun `findById returns null when not found`() {
        trxManager.run {
            assertNull(repoUsers.findById(999))
        }
    }

    @Test
    fun `findById returns null on empty repo`() {
        trxManager.run {
            assertNull(repoUsers.findById(1))
        }
    }

    @Test
    fun `findByEmail returns correct user`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val found = repoUsers.findByEmail("alice@gmail.com")
            assertNotNull(found)
            assertEquals(user.id, found.id)
        }
    }

    @Test
    fun `findByEmail returns null when not found`() {
        trxManager.run {
            assertNull(repoUsers.findByEmail("notfound@gmail.com"))
        }
    }

    @Test
    fun `findByEmail is case sensitive`() {
        trxManager.run {
            repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            assertNull(repoUsers.findByEmail("Alice@gmail.com"))
            assertNull(repoUsers.findByEmail("ALICE@GMAIL.COM"))
        }
    }

    @Test
    fun `findAll returns empty on empty repo`() {
        trxManager.run {
            assertEquals(emptyList(), repoUsers.findAll())
        }
    }

    @Test
    fun `findByRole returns correct users`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"), UserRole.ADMIN)
            val u3 = repoUsers.createUser("Carol", "carol@gmail.com", PasswordValidationInfo("hash3"), UserRole.USER)
            val USERs = repoUsers.findByRole(UserRole.USER)
            val admins = repoUsers.findByRole(UserRole.ADMIN)
            assertEquals(2, USERs.size)
            assertTrue(USERs.any { it.id == u1.id })
            assertTrue(USERs.any { it.id == u3.id })
            assertEquals(1, admins.size)
            assertTrue(admins.any { it.id == u2.id })
        }
    }

    @Test
    fun `findByRole returns empty when no users with that role`() {
        trxManager.run {
            repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            assertEquals(emptyList(), repoUsers.findByRole(UserRole.ADMIN))
        }
    }

    @Test
    fun `findByRole returns empty on empty repo`() {
        trxManager.run {
            assertEquals(emptyList(), repoUsers.findByRole(UserRole.USER))
            assertEquals(emptyList(), repoUsers.findByRole(UserRole.ADMIN))
        }
    }

    @Test
    fun `updateRole changes user role`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val updated = repoUsers.updateRole(user, UserRole.ADMIN)
            assertEquals(UserRole.ADMIN, updated.role)
        }
    }

    @Test
    fun `updateRole persists changes`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            repoUsers.updateRole(user, UserRole.ADMIN)
            assertEquals(UserRole.ADMIN, repoUsers.findById(user.id)?.role)
        }
    }

    @Test
    fun `updateRole does not affect other users`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"), UserRole.USER)
            repoUsers.updateRole(u1, UserRole.ADMIN)
            assertEquals(UserRole.USER, repoUsers.findById(u2.id)?.role)
        }
    }

    @Test
    fun `updateRole with same role does nothing`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val updated = repoUsers.updateRole(user, UserRole.USER)
            assertEquals(UserRole.USER, updated.role)
            assertEquals(UserRole.USER, repoUsers.findById(user.id)?.role)
        }
    }

    @Test
    fun `save updates existing user`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val updated = user.copy(name = "AliceUpdated", email = "updated@gmail.com")
            repoUsers.save(updated)
            val found = repoUsers.findById(user.id)
            assertEquals("AliceUpdated", found?.name)
            assertEquals("updated@gmail.com", found?.email)
        }
    }

    @Test
    fun `save updates password validation`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("oldhash"), UserRole.USER)
            val updated = user.copy(passwordValidation = PasswordValidationInfo("newhash"))
            repoUsers.save(updated)
            val found = repoUsers.findById(user.id)
            assertEquals(PasswordValidationInfo("newhash"), found?.passwordValidation)
        }
    }

    @Test
    fun `save does not duplicate user`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            repoUsers.save(user.copy(name = "Updated"))
            assertEquals(1, repoUsers.findAll().size)
        }
    }

    @Test
    fun `deleteById removes user`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            repoUsers.deleteById(user.id)
            assertNull(repoUsers.findById(user.id))
        }
    }

    @Test
    fun `deleteById only removes the correct user`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"), UserRole.USER)
            repoUsers.deleteById(u1.id)
            assertNull(repoUsers.findById(u1.id))
            assertNotNull(repoUsers.findById(u2.id))
        }
    }

    @Test
    fun `deleteById on nonexistent id does nothing`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            repoUsers.deleteById(999)
            assertNotNull(repoUsers.findById(user.id))
        }
    }

    @Test
    fun `createToken and getTokenByTokenValidationInfo`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val info = TokenValidationInfo("token123")
            val now = Instant.now()
            val token = Token(info, user.id, now, now)
            repoUsers.createToken(token, maxTokens = 2)
            val result = repoUsers.getTokenByTokenValidationInfo(info)
            assertNotNull(result)
            assertEquals(user.id, result.first.id)
            assertEquals(info, result.second.tokenValidationInfo)
        }
    }

    @Test
    fun `createToken removes oldest when maxTokens exceeded`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val init = Instant.now().minusSeconds(60)
            val t1 = Token(TokenValidationInfo("t1"), user.id, init, Instant.now().minusSeconds(10))
            val t2 = Token(TokenValidationInfo("t2"), user.id, init, Instant.now().minusSeconds(5))
            val t3 = Token(TokenValidationInfo("t3"), user.id, init, Instant.now())
            repoUsers.createToken(t1, maxTokens = 2)
            repoUsers.createToken(t2, maxTokens = 2)
            repoUsers.createToken(t3, maxTokens = 2)
            assertNull(repoUsers.getTokenByTokenValidationInfo(TokenValidationInfo("t1")))
            assertNotNull(repoUsers.getTokenByTokenValidationInfo(TokenValidationInfo("t2")))
            assertNotNull(repoUsers.getTokenByTokenValidationInfo(TokenValidationInfo("t3")))
        }
    }

    @Test
    fun `createToken does not remove tokens of other users`() {
        trxManager.run {
            val u1 = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val u2 = repoUsers.createUser("Bob", "bob@gmail.com", PasswordValidationInfo("hash2"), UserRole.USER)
            val init = Instant.now().minusSeconds(60)
            val t1 = Token(TokenValidationInfo("t1"), u1.id, init, Instant.now().minusSeconds(10))
            val t2 = Token(TokenValidationInfo("t2"), u1.id, init, Instant.now().minusSeconds(5))
            val t3 = Token(TokenValidationInfo("t3"), u2.id, init, Instant.now())
            repoUsers.createToken(t1, maxTokens = 1)
            repoUsers.createToken(t2, maxTokens = 1)
            repoUsers.createToken(t3, maxTokens = 1)
            assertNull(repoUsers.getTokenByTokenValidationInfo(TokenValidationInfo("t1")))
            assertNotNull(repoUsers.getTokenByTokenValidationInfo(TokenValidationInfo("t2")))
            assertNotNull(repoUsers.getTokenByTokenValidationInfo(TokenValidationInfo("t3")))
        }
    }

    @Test
    fun `updateTokenLastUsed updates the timestamp`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val info = TokenValidationInfo("tokenAlice")
            val init = Instant.now().minusSeconds(200)
            val oldToken = Token(info, user.id, init, Instant.now().minusSeconds(100))
            repoUsers.createToken(oldToken, maxTokens = 2)
            val newLastUsed = Instant.now()
            val newToken = Token(info, user.id, init, newLastUsed)
            repoUsers.updateTokenLastUsed(newToken, newLastUsed)
            val result = repoUsers.getTokenByTokenValidationInfo(info)
            assertNotNull(result)
            assertEquals(newLastUsed.epochSecond, result.second.lastUsedAt.epochSecond)
        }
    }

    @Test
    fun `removeTokenByValidationInfo removes token and returns count`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val info = TokenValidationInfo("tokenAlice")
            val now = Instant.now()
            repoUsers.createToken(Token(info, user.id, now, now), maxTokens = 2)
            val removed = repoUsers.removeTokenByValidationInfo(info)
            assertEquals(1, removed)
            assertNull(repoUsers.getTokenByTokenValidationInfo(info))
        }
    }

    @Test
    fun `removeTokenByValidationInfo returns 0 when token not found`() {
        trxManager.run {
            assertEquals(0, repoUsers.removeTokenByValidationInfo(TokenValidationInfo("nonexistent")))
        }
    }

    @Test
    fun `removeTokenByValidationInfo only removes the correct token`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val info1 = TokenValidationInfo("token1")
            val info2 = TokenValidationInfo("token2")
            val now = Instant.now()
            repoUsers.createToken(Token(info1, user.id, now, now), maxTokens = 5)
            repoUsers.createToken(Token(info2, user.id, now, now), maxTokens = 5)
            repoUsers.removeTokenByValidationInfo(info1)
            assertNull(repoUsers.getTokenByTokenValidationInfo(info1))
            assertNotNull(repoUsers.getTokenByTokenValidationInfo(info2))
        }
    }

    @Test
    fun `clear removes all users and tokens`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@gmail.com", PasswordValidationInfo("hash"), UserRole.USER)
            val info = TokenValidationInfo("tokenAlice")
            val now = Instant.now()
            repoUsers.createToken(Token(info, user.id, now, now), maxTokens = 2)
            repoUsers.clear()
            assertEquals(emptyList(), repoUsers.findAll())
            assertNull(repoUsers.getTokenByTokenValidationInfo(info))
        }
    }

    @Test
    fun `clear on empty repo does nothing`() {
        trxManager.run {
            repoUsers.clear()
            assertEquals(emptyList(), repoUsers.findAll())
        }
    }
}
