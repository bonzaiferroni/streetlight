package streetlight.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import streetlight.app.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LottieSpirit(
    spiritName: String,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        if (spiritName.endsWith(".json")) {
            LottieCompositionSpec.JsonString(
                Res.readBytes("files/$spiritName").decodeToString()
            )
        } else {
            LottieCompositionSpec.DotLottie(
                Res.readBytes("files/$spiritName.lottie")
            )
        }
    }

    Image(
        painter = rememberLottiePainter(
            composition = composition,
            iterations = Compottie.IterateForever
        ),
        contentDescription = "Lottie animation",
        modifier = modifier.fillMaxWidth()
    )
}