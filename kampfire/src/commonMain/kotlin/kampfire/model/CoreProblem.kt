package kampfire.model

import io.ktor.http.HttpStatusCode

/** Problems shared across domains. [Something] is the catchall for an unexpected error. */
object CoreProblem {
    val NotImplemented = Problem("This feature is not yet implemented.")
    val Something = Problem("The server ran into a problem.")
    val InvalidSlug = Problem("A slug can have only letters, numbers, and hyphens.")
}

/** Problems of signing in and managing an account. */
object AuthProblem {
    val PasswordRequired = Problem("A password is required.")
    val InvalidPassword = Problem("Invalid password")
    val AccountLocked = Problem("This account was locked. Check your email for a link to set a new password.")
    val CurrentPasswordInvalid = Problem("Your current password is not correct.")
    val NewPasswordRequired = Problem("Please enter a new password.")
}

/** Problems named for an HTTP status. */
object HttpProblem {
    val NotAuthorized = Problem("Not authorized.")
    val TooManyRequests = Problem("Too many requests.")
    val Conflict = Problem("There was a conflict.")
    val InternalServerError = CoreProblem.Something
    val NotFound = Problem("That resource doesn't exist.")
    val BadRequest = Problem("The request was invalid.")
}

/** The [HttpProblem] for this status, or a generic one naming it. */
fun HttpStatusCode.toProblem() = this.value.toHttpProblem()

/** The [HttpProblem] for this status code, or a generic one naming it. */
fun Int.toHttpProblem() = when (this) {
    401 -> HttpProblem.NotAuthorized
    429 -> HttpProblem.TooManyRequests
    409 -> HttpProblem.Conflict
    500 -> HttpProblem.InternalServerError
    else -> Problem("HTTP error: $this")
}