package streetlight.model.data

/** Whether a post was made or already existed. */
enum class PostResult {
    Posted,
    Conflict,
}