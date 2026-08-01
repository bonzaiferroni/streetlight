package kampfire.model

object AuthProblem {
    val PasswordRequired = Problem("A password is required.")
    val InvalidPassword = Problem("Invalid password")
    val AccountLocked = Problem("This account was locked. Check your email for a link to set a new password.")
    val CurrentPasswordInvalid = Problem("Your current password is not correct.")
    val NewPasswordRequired = Problem("Please enter a new password.")
}

object HttpProblem {
    val NotAuthorized = Problem("Not authorized.")
    val TooManyRequests = Problem("Too many requests.")
    val Conflict = Problem("There was a conflict.")
    val InternalServerError = Problem("The server ran into a problem.")
}