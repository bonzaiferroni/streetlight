package kampfire.model

/** A role granted to an account. */
enum class UserRole {
	Admin,
	User,
	Bot;

	companion object {
		private val map = entries.associateBy { it.ordinal }

		/** The role with this ordinal. Throws for an unknown ordinal. */
		fun Int.toUserRole() = map[this] ?: throw IllegalArgumentException("UserRole has no ordinal value: $this")
	}
}

/** The roles as a comma-separated claim, read back with [toUserRoleSet]. */
fun RoleSet.toClaimValue() = this.joinToString(",")
/** Reads roles written by [toClaimValue]. */
fun String.toUserRoleSet() = this.split(",").map { UserRole.valueOf(it) }.toSet()

typealias RoleSet = Set<UserRole>