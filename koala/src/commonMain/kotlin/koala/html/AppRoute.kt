package koala.html

import kampfire.api.Slug
import kampfire.api.Username
import kampfire.api.toSlug
import kampfire.api.toUsername
import kampfire.model.Labeled
import kotlin.uuid.Uuid

/** A place in the app, with the [screen] that renders it and the path that reaches it. */
interface AppRoute: Labeled {
    val screen: AppScreen
    val origin: String
    fun toRelativePath() = basePath
    fun toAbsolutePath() = "$origin${toRelativePath()}"
    val title: String // td initialize as screen title
    val basePath get() = screen.pathBase
    override val label get() = title

    companion object {
        /**
         * The route [sitePath] leads to, parsed by the [AppScreen] whose path root it starts with, or `null`
         * when none matches.
         */
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

/**
 * A kind of page in the app, with the path root it is reached by and the [routeParse] that turns a path into its
 * route.
 */
interface AppScreen {
    val pathRoot: String
    val pathBase: String
    val routeParse: RouteParse
    val screenId: String
    val hasShell: Boolean
    val retainWithinScreen: Boolean
    // td: add title
}

private fun String.dropStart(char: Char) = if (startsWith(char)) drop(1) else this

/** Turns the segments of a path into an [AppRoute], by the shape of its argument. */
sealed interface RouteParse {
    val label: String get() = "id"
}

/** A route with no argument. */
data class StaticParse(
    val block: () -> AppRoute
): RouteParse

/** A route with a [Uuid] argument. */
data class UuidParse(
    val block: (Uuid) -> AppRoute
): RouteParse

/** A route with a text id argument. */
data class IdParse(
    val block: (String) -> AppRoute
): RouteParse

/** A route with an optional [Slug] argument. */
data class SlugOrNullParse(
    val block: (Slug?) -> AppRoute
): RouteParse

/** A route with a [Slug] argument. */
data class SlugParse(
    val block: (Slug) -> AppRoute
): RouteParse

/** A route with a [Username] argument. */
data class UsernameParse(
    val block: (Username) -> AppRoute
): RouteParse

/** A route read from all its path segments, reachable under each of [roots]. */
data class SegmentParse(
    val roots: List<String>,
    val block: (List<String>) -> AppRoute
): RouteParse