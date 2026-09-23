package streetlight.web.io

import kampfire.api.Slug
import kampfire.model.Outcome
import streetlight.model.data.*

class TestCityClient: CityClient {
    override suspend fun readCity(slug: Slug): Outcome<City> = TODO()
    override suspend fun readTopCities(): Outcome<List<City>> = TODO()
    override suspend fun readCityPosts(slug: Slug): Outcome<List<Entity>> = TODO()
    override suspend fun readCityContent(slug: Slug): Outcome<CityContent> = TODO()
    override suspend fun updateCity(edit: CityEdit): Outcome<City> = TODO()
    override suspend fun readCityFeed(cityId: CityId, cursor: EntityCursor?): Outcome<EntityFeed> = TODO()
    override suspend fun searchCity(query: String, country: String): Outcome<List<City>> = TODO()
}
