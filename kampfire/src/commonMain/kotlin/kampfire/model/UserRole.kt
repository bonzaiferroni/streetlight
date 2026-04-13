package kampfire.model

enum class UserRole {
	Admin,
	User,
	Bot;

	companion object {
		private val map = entries.associateBy { it.ordinal }

		fun Int.toUserRole() = map[this] ?: throw IllegalArgumentException("UserRole has no ordinal value: $this")
	}
}

fun RoleSet.toClaimValue() = this.joinToString(",")
fun String.toUserRoleSet() = this.split(",").map { UserRole.valueOf(it) }.toSet()

typealias RoleSet = Set<UserRole>