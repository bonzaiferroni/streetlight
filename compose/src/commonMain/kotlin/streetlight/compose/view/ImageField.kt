package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import koala.SvgFile
import kotlinx.coroutines.launch
import streetlight.compose.model.ImageEditor
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.utility.collectAsState

/**
 * The editor's image, the one picked or else the one stored, contained and held to [PreviewHeightUnits], and the
 * buttons to choose another from the device, with the system's own picker, a file dialog on a desktop and the photo
 * picker on a phone, and to remove it. The cousin of the web's `imageFormSection` and its image drop.
 */
@Composable
context(editor: ImageEditor)
fun ImageField() {
    val theme = currentTheme
    val scope = rememberCoroutineScope()
    val image by editor.imageState.collectAsState()
    val picked by editor.pickedState.collectAsState()
    val launcher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file ?: return@rememberFilePickerLauncher
        scope.launch { editor.pick(file.name, file.readBytes()) }
    }
    val preview: Any? = picked?.bytes ?: image?.url

    Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        preview?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
                    .heightIn(max = theme.unit * PreviewHeightUnits)
                    .borderRadius(1, outline = Outline.Mid),
            )
        }
        Row(1) {
            Button("choose image", onClick = { launcher.launch() }, flair = SvgFile.Photo, style = ButtonStyle.Zen)
            if (preview != null) Button("remove", onClick = editor::clear, style = ButtonStyle.Zen)
        }
    }
}

/** The tallest the preview runs in units, the web's `MaxHeight(64)`. */
private const val PreviewHeightUnits = 64
