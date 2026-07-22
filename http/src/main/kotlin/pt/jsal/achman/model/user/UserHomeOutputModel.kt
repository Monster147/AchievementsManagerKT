package pt.jsal.achman.model.user

import pt.jsal.achman.user.UserRole

data class UserHomeOutputModel(
    val id: Int,
    val name: String,
    val email: String,
    val role: UserRole
)
