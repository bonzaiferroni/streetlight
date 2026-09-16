package koala.modifier

import koala.html.AppScreen
import koala.html.Id

object KoalaBody {
    val PortalMount = Id("portal-mount")
    val ShellMount = Id("shell-mount")

    val ScreenId = stringAttributeOf("screen-id", true)
}

fun generateScreenSelectors(screens: List<AppScreen>): String {
    return buildString {
        screens.forEachIndexed { index, screen ->
            val screenId = screen.screenId
            val valueSelector = "[${KoalaBody.ScreenId.identifier}='$screenId']"
            append("body$valueSelector $valueSelector")
            if (index + 1 < screens.size) {
                append(",\n")
            }
        }
    }
}