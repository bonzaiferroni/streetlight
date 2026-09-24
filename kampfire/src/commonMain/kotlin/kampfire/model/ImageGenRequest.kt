package kampfire.model

/** A request to generate an image from a text prompt, with an optional [theme] and the [filename] to save it under. */
class ImageGenRequest(
    val text: String,
    val theme: String? = null,
    val filename: String? = null,
)