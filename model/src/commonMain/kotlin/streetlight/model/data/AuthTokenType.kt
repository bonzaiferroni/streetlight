package streetlight.model.data

/** The purposes a token sent by email serves, such as verifying an address or resetting a password. */
enum class AuthTokenType {
    EmailVerification,
    PasswordReset,
    AccountLockdown,
    AccountNotOwned,
}