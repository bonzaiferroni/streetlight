package koala.html

import kampfire.api.Slug
import kampfire.api.Username
import kampfire.api.toSlug
import kampfire.api.toUsername
import kampfire.model.Labeled
import kotlin.uuid.Uuid

interface AppRoute: Labeled {
    val screen: AppScreen
    val origin: String
    fun toRelativePath() = basePath
    fun toAbsolutePath() = "$origin${toRelativePath()}"
    val title: String
    val basePath get() = "/${screen.pathRoot}"
    override val label get() = title

    companion object {
        fun routeOf(sitePath: String, screens: List<AppScreen>): AppRoute? {
            val fragment = sitePath.dropStart('/')
            val segments = fragment.split('/')
            val root = segments[0].lowercase()
            val screen = screens.firstOrNull { it.pathRoot == root } ?: return null

            val idArg = segments.getOrNull(1)
            return when (val parse = screen.routeParse) {
                is StaticParse -> parse.block()
                is UuidParse -> idArg?.let { parse.block(Uuid.parse(it)) }
                is IdParse -> idArg?.let { parse.block(it) }
                is SlugOrNullParse -> parse.block(idArg?.toSlug())
                is SlugParse -> idArg?.let { parse.block(idArg.toSlug()) }
                is UsernameParse -> idArg?.let { parse.block(idArg.toUsername()) }
                is SegmentParse -> parse.block(segments)
            }
        }
    }
}

interface AppScreen {
    val pathRoot: String
    val routeParse: RouteParse
    val screenId: String
    val hasShell: Boolean
    val retainWithinScreen: Boolean
}

private fun String.dropStart(char: Char) = if (startsWith(char)) drop(1) else this

sealed interface RouteParse {
    val label: String get() = "id"
}

data class StaticParse(
    val block: () -> AppRoute
): RouteParse

data class UuidParse(
    val block: (Uuid) -> AppRoute
): RouteParse

data class IdParse(
    val block: (String) -> AppRoute
): RouteParse

data class SlugOrNullParse(
    val block: (Slug?) -> AppRoute
): RouteParse

data class SlugParse(
    val block: (Slug) -> AppRoute
): RouteParse

data class UsernameParse(
    val block: (Username) -> AppRoute
): RouteParse

data class SegmentParse(
    val roots: List<String>,
    val block: (List<String>) -> AppRoute
): RouteParse