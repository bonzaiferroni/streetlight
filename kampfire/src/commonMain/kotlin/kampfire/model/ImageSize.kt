package kampfire.model

enum class ImageSize(val label: String, val widthPx: Int, val minWidthPx: Int, val aspectRatio: Float?) {
    Thumb("th", THUMB_IMAGE_SIZE, 0, 1f),
    Small("sm", SMALL_IMAGE_SIZE, 0, null),
    Medium("md", MEDIUM_IMAGE_SIZE, SMALL_IMAGE_SIZE, null),
    Large("lg", LARGE_IMAGE_SIZE, MEDIUM_IMAGE_SIZE, null);

    companion object {
        val All = listOf(Thumb, Small, Medium, Large)
    }
}

const val THUMB_IMAGE_SIZE = 128
const val SMALL_IMAGE_SIZE = 256
const val MEDIUM_IMAGE_SIZE = 540
const val LARGE_IMAGE_SIZE = 1080