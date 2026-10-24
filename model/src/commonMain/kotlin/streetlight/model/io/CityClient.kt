package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.Slug
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.*

/** The calls of `Api.Cities`. */
class CityClient(private val transport: ApiTransport) {
    suspend fun readCity(slug: Slug): Outcome<City> = transport.getApi(Api.Cities.ReadCity, slug)
    suspend fun readTopCities(): Outcome<List<City>> = transport.getApi(Api.Cities.ReadTopCities)
    suspend fun readCityPosts(slug: Slug): Outcome<List<Entity>> = transport.getApi(Api.Cities.ReadCityPosts, slug)
    suspend fun readCityContent(slug: Slug, feed: FeedType?): Outcome<CityContent> = transport.getApi(Api.Cities.ReadContent, slug) { endpoint ->
        feed?.let { writeParam(endpoint.feed, it) }
    }
    suspend fun updateCity(edit: CityEdit): Outcome<City> = transport.postApi(Api.Cities.UpdateCity, edit)
    suspend fun searchCity(query: String, country: String): Outcome<List<City>> =
        transport.getApi(Api.Cities.Search) {
            writeParam(it.query, query)
            writeParam(it.country, country)
        }
}
