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
            val role = Role.entries.find { it.v == v }
            if (role == null) {
                throw IllegalArgumentException("Invalid role: '$v'. Available roles are: ${entries.joinToString { it.v.toString() }}")
            }
            return role
        }
    }
}
