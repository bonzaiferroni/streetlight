package streetlight.compose.io

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.map.Mapper
import coil3.network.ktor3.KtorNetworkFetcherFactory
import kampfire.model.Url
import streetlight.compose.model.AppEnv

/** The app's image loader, which loads a [Url] and resolves a relative one against the server of [env]. */
fun buildImageLoader(context: PlatformContext, env: AppEnv) = ImageLoader.Builder(context)
    .components {
        add(Mapper<Url, String> { url, _ -> if (url.isRelative) env.apiUrl + url.value else url.value })
        add(KtorNetworkFetcherFactory())
    }
    .build()
