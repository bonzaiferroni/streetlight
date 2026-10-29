package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kampfire.model.Tap
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.SvgFile
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/** The message of [tap], or nothing while it is `null`. The cousin of the web's `messageBox`. */
@Composable
fun MessageBox(tap: Tap<UIMessage?>, modifier: Modifier = Modifier) {
    val message by tap.collectAsState()
    message?.let { MessageCard(it, modifier.borderRadius(MessageRadiusUnits, moonShadow = true)) }
}

/** [message] beside the icon of its type, on the background of its type. */
@Composable
fun MessageCard(message: UIMessage, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val colors = theme.colors
    val background = when (message.messageType) {
        UIMessageType.Error -> colors.errorBg
        UIMessageType.Success -> colors.successBg
        UIMessageType.Info, UIMessageType.Working -> colors.primaryCardBg
    }
    val ink = { colors.ink }

    Row(1, modifier.background(background).padding(1), verticalAlignment = Alignment.CenterVertically) {
        when (message.messageType) {
            UIMessageType.Info -> Icon(SvgFile.Info.small, ink)
            UIMessageType.Error -> Icon(SvgFile.AlertTriangle.small, ink)
            UIMessageType.Success -> Icon(SvgFile.CircleCheck.small, ink)
            UIMessageType.Working -> AnimatedIcon(SvgFile.Loader.animated, ink)
        }
        BasicText(message.text, Modifier.weight(1f, fill = false), style = theme.text.regular, color = ink)
    }
}

/** The rounding of a message's corners in units, less than a button's, as the web's. */
const val MessageRadiusUnits = 1
