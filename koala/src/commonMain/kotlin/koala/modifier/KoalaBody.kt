package koala.modifier

import koala.html.AppScreen
import koala.html.Id

/** The ids and attribute of the app body that Koala mounts into and reads. */
object KoalaBody {
    val PortalMount = Id("portal-mount")
    val ShellMount = Id("shell-mount")

    val ScreenId = stringAttributeOf("screen-id", true)
}

/** A selector list matching, for each of [screens], an element marked with that screen inside a body showing it. */
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