package kampfire.model

enum class ImageSize(val widthPx: Int, val minWidthPx: Int) {
    Small(SMALL_IMAGE_SIZE, 0),
    Medium(MEDIUM_IMAGE_SIZE, SMALL_IMAGE_SIZE),
    Large(LARGE_IMAGE_SIZE, MEDIUM_IMAGE_SIZE);
}

const val SMALL_IMAGE_SIZE = 128
const val MEDIUM_IMAGE_SIZE = 512
const val LARGE_IMAGE_SIZE = 1024