package pt.jsal.achman

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.jsal.achman.model.Problem
import pt.jsal.achman.model.user.UserHomeOutputModel
import pt.jsal.achman.model.user.UserInput
import pt.jsal.achman.model.user.UserLoginInputModel
import pt.jsal.achman.model.user.UserLoginOutputModel
import pt.jsal.achman.user.AuthenticatedUser
import pt.jsal.achman.user.User
import pt.jsal.achman.utils.Either
import pt.jsal.achman.utils.Failure
import pt.jsal.achman.utils.Success

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @PostMapping
    fun createUser(
        @RequestBody userInput: UserInput,
    ): ResponseEntity<*> {
        val result: Either<UserError, User> =
            userService.createUser(
                name = userInput.name,
                email = userInput.email,
                password = userInput.password,
            )

        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(
                        "Location",
                        "/api/user/${result.value.id}",
                    ).build<Unit>()

            is Failure ->
                when (result.value) {
                    is UserError.AlreadyUsedEmailAddress ->
                        Problem.EmailAlreadyInUse.response(
                            HttpStatus.BAD_REQUEST,
                        )

                    is UserError.InsecurePassword ->
                        Problem.InsecurePassword.response(
                            HttpStatus.BAD_REQUEST,
                        )

                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestBody loginInput: UserLoginInputModel,
    ): ResponseEntity<*> {
        val result =
            userService.createToken(
                loginInput.email,
                loginInput.password,
            )
        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(UserLoginOutputModel(result.value.tokenValue))

            is Failure ->
                when (result.value) {
                    TokenCreationError.UserOrPasswordAreInvalid ->
                        Problem.UserOrPasswordAreInvalid.response(HttpStatus.BAD_REQUEST)
                }
        }
    }

    @PostMapping("/logout")
    fun logout(user: AuthenticatedUser) {
        userService.revokeToken(user.token)
    }

    @GetMapping("/me")
    fun userHome(userAuthenticatedUser: AuthenticatedUser): ResponseEntity<UserHomeOutputModel> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                UserHomeOutputModel(
                    userAuthenticatedUser.user.id,
                    userAuthenticatedUser.user.name,
                    userAuthenticatedUser.user.email,
                ),
            )
}
