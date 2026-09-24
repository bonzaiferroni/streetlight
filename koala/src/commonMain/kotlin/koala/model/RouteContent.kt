package koala.model

/** Content a [ContentFetcher] returns for a route. */
sealed interface FetcherContent

/** The content of a route, read by its view through routeBlock. */
interface RouteContent : FetcherContent

/** Stands for a route that has no content to fetch. */
object NullContent: FetcherContent

/** This content as [T], or `null` when it is another type. */
inline fun <reified T> FetcherContent.toContentOrNull(): T? = this as? T