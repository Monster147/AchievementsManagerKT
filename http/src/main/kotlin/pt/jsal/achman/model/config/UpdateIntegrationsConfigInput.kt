package pt.jsal.achman.model.config

data class UpdateIntegrationsConfigInput(
    val steamApiKey: String?,
    val steamUserId: String?,
    val retroApiKey: String?,
    val retroUsername: String?,
    val psnApiKey: String?,
)
