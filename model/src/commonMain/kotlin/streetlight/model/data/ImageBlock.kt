package streetlight.model.data

import kampfire.model.Labeled
import koala.Image
import kotlinx.serialization.Serializable

/** A layout block showing an image, with its shape, fit and width. */
@Serializable
data class ImageBlock(
    val image: Image?,
    val shape: ImageShape? = null,
    val fit: ObjectFit? = null,
    val width: Int? = null,
): LayoutBlock {
    override val blockType get() = BlockType.Image
}

/** The shapes an image can be cut to in a layout. */
enum class ImageShape: Labeled {
    Square, Rounded, Circle, Ellipse, Pill, Chopped;
    override val label get() = name

    companion object {
        val default = Rounded
    }
}

/** How an image fills its frame in a layout. */
enum class ObjectFit(label: String? = null): Labeled {
    Fill, Stretch, Contain, Cover, ScaleDown("Scale Down");
    override val label = label ?: name

    companion object {
        val default = Fill
    }
}

/** A layout block showing images in columns. */
@Serializable
data class GalleryBlock(
    val images: List<Image>,
    val columns: Int = 2,
    val shape: ImageShape? = null,
): LayoutBlock {
    override val blockType get() = BlockType.Gallery
}