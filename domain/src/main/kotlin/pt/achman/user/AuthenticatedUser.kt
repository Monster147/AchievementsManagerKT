package pt.achman.user

class AuthenticatedUser(
    val user: User,
    val token: String,
)
