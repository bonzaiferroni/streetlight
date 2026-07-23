package koala.model

sealed interface FetcherContent

interface RouteContent : FetcherContent

object NullFetcherContent: FetcherContent

inline fun <reified T> FetcherContent.toContentOrNull(): T? = this as? T