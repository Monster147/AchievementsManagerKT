package pt.achman

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.jsal.achman.TokenCreationError
import pt.jsal.achman.UserError
import pt.jsal.achman.UserService
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.utils.Either
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class UserServiceTest {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var trxManager: TransactionManager

    @BeforeEach
    fun reset() {
        trxManager.run {
            repoUsers.clear()
        }
    }

    @Test
    fun `createUser stores user and encodes password`() {
        val result = userService.createUser("Alice", "alice@mail.com", "Password1!")

        assertTrue(result is Either.Right)
        val user = result.value

        assertEquals("Alice", user.name)
        assertEquals("alice@mail.com", user.email)
        assertTrue(passwordEncoder.matches("Password1!", user.passwordValidation.validationInfo))
    }

    @Test
    fun `createUser fails if email already exists`() {
        userService.createUser("Alice", "alice@mail.com", "Password1!")

        val result = userService.createUser("Alice2", "alice@mail.com", "Password1!")

        assertTrue(result is Either.Left)
        assertTrue(result.value is UserError.AlreadyUsedEmailAddress)
    }

    @Test
    fun `createUser fails if password is insecure`() {
        val result = userService.createUser("Bob", "bob@mail.com", "123")

        assertTrue(result is Either.Left)
        assertTrue(result.value is UserError.InsecurePassword)
    }

    @Test
    fun `findUserById returns user`() {
        val user =
            userService.createUser("Bob", "bob@mail.com", "Password1!").let {
                (it as Either.Right).value
            }
        val found = userService.findUserById(user.id)
        assertNotNull(found)
        assertTrue(found is Either.Right)
        assertEquals(user.id, found.value.id)
    }

    @Test
    fun `findUserById returns error if id does not exist`() {
        val result = userService.findUserById(999)

        assertTrue(result is Either.Left)
        assertTrue(result.value is UserError.UserNotFound)
    }

    @Test
    fun `deleteUser returns success`() {
        val user =
            userService.createUser("Bob", "bob@mail.com", "Password1!").let {
                check(it is Either.Right)
                it.value
            }
        val result = userService.deleteUser(user.id)
        assertTrue(result is Either.Right)
        assertTrue(result.value)
    }

    @Test
    fun `deleteUser fails if user does not exist`() {
        val result = userService.deleteUser(999)
        assertTrue(result is Either.Left)
        assertTrue(result.value is UserError.UserNotFound)
    }

    @Test
    fun `isSafePassword returns true for valid password`() {
        assertTrue(userService.isSafePassword("Password1!"))
    }

    @Test
    fun `isSafePassword returns false for invalid password`() {
        assertFalse(userService.isSafePassword("pass"))
        assertFalse(userService.isSafePassword("password"))
        assertFalse(userService.isSafePassword("PASSWORD1"))
        assertFalse(userService.isSafePassword("Password"))
    }

    @Test
    fun `createToken returns token for valid credentials`() {
        userService.createUser("Alice", "alice@mail.com", "Password1!")

        val result = userService.createToken("alice@mail.com", "Password1!")

        assertTrue(result is Either.Right)
        val token = result.value

        assertTrue(token.tokenValue.isNotBlank())
        assertTrue(token.tokenExpiration.isAfter(Instant.now()))
    }

    @Test
    fun `createToken fails with wrong password`() {
        userService.createUser("Alice", "alice@mail.com", "Password1!")

        val result = userService.createToken("alice@mail.com", "wrong")

        assertTrue(result is Either.Left)
        assertTrue(result.value is TokenCreationError.UserOrPasswordAreInvalid)
    }

    @Test
    fun `createToken fails with unknown email`() {
        val result = userService.createToken("unknown@mail.com", "Password1!")

        assertTrue(result is Either.Left)
        assertTrue(result.value is TokenCreationError.UserOrPasswordAreInvalid)
    }

    @Test
    fun `createToken fails with blank credentials`() {
        val result = userService.createToken("", "")

        assertTrue(result is Either.Left)
        assertTrue(result.value is TokenCreationError.UserOrPasswordAreInvalid)
    }

    @Test
    fun `getUserByToken returns user for valid token`() {
        val user =
            userService.createUser("Alice", "alice@mail.com", "Password1!").let {
                (it as Either.Right).value
            }

        val token =
            userService.createToken("alice@mail.com", "Password1!").let {
                (it as Either.Right).value
            }

        val found = userService.getUserByToken(token.tokenValue)

        assertNotNull(found)
        assertEquals(user.id, found.id)
    }

    @Test
    fun `getUserByToken returns null for invalid token`() {
        val result = userService.getUserByToken("invalid-token")

        assertNull(result)
    }

    @Test
    fun `getUserByToken updates lastUsedAt implicitly`() {
        userService.createUser("Alice", "alice@mail.com", "Password1!")

        val token =
            userService.createToken("alice@mail.com", "Password1!").let {
                (it as Either.Right).value
            }

        val first = userService.getUserByToken(token.tokenValue)
        val second = userService.getUserByToken(token.tokenValue)

        assertNotNull(first)
        assertNotNull(second)
    }

    @Test
    fun `revokeToken removes token`() {
        userService.createUser("Alice", "alice@mail.com", "Password1!")

        val token =
            userService.createToken("alice@mail.com", "Password1!").let {
                (it as Either.Right).value
            }

        assertTrue(userService.revokeToken(token.tokenValue))

        val found = userService.getUserByToken(token.tokenValue)
        assertNull(found)
    }

    @Test
    fun `revokeToken on nonexistent token still returns true`() {
        assertTrue(userService.revokeToken("nonexistent"))
    }
}
