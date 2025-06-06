package ru.yarsu.enums

enum class Role(
    val v: String,
) {
    USER("User"),
    TEMPLATE_MANAGER("TemplateManager"),
    USER_MANAGER("UserManager"),
    ;

    companion object {
        fun fromString(v: String): Role {
            return try {
                Role.valueOf(v)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid role: '$v'. Available roles are: ${entries.joinToString()}")
            }
        }
    }
}
