package streetlight.compose.io

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.map.Mapper
import coil3.util.DebugLogger
import coil3.network.ktor3.KtorNetworkFetcherFactory
import kampfire.model.Url
import streetlight.compose.model.AppConfig

/** The app's image loader, which loads a [Url] and resolves a relative one against the server at [config]. */
fun buildImageLoader(context: PlatformContext, config: AppConfig) = ImageLoader.Builder(context)
    .components {
        add(Mapper<Url, String> { url, _ -> if (url.isRelative) config.apiUrl + url.value else url.value })
        add(KtorNetworkFetcherFactory())
    }
    // td: remove once the low-resolution first load of a focused image is understood
    .logger(DebugLogger())
    .build()
