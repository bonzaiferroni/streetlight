package streetlight.web.io

import kampfire.api.Slug
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.*

/** The calls of `Api.Cities`. */
interface CityClient {
    suspend fun readCity(slug: Slug): Outcome<City>
    suspend fun readTopCities(): Outcome<List<City>>
    suspend fun readCityPosts(slug: Slug): Outcome<List<Entity>>
    suspend fun readCityContent(slug: Slug, feed: FeedType?): Outcome<CityContent>
    suspend fun updateCity(edit: CityEdit): Outcome<City>
    suspend fun searchCity(query: String, country: String): Outcome<List<City>>
}

class BrowserCityClient(private val client: FetchClient): CityClient {
    override suspend fun readCity(slug: Slug) = client.getApi(Api.Cities.ReadCity, slug)
    override suspend fun readTopCities() = client.getApi(Api.Cities.ReadTopCities)
    override suspend fun readCityPosts(slug: Slug) = client.getApi(Api.Cities.ReadCityPosts, slug)
    override suspend fun readCityContent(slug: Slug, feed: FeedType?) = client.getApi(Api.Cities.ReadContent, slug) { endpoint ->
        feed?.let { writeParam(endpoint.feed, it) }
    }
    override suspend fun updateCity(edit: CityEdit) = client.postApi(Api.Cities.UpdateCity, edit)
    override suspend fun searchCity(query: String, country: String) =
        client.getApi(Api.Cities.Search) {
            writeParam(it.query, query)
            writeParam(it.country, country)
        }
}
