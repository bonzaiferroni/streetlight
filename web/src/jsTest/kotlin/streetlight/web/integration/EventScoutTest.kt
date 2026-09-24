package streetlight.web.integration

import koala.model.GeoCamera
import koala.model.GeoMap
import streetlight.model.data.EventEdit
import streetlight.model.data.LocationEdit
import streetlight.web.io.ApiClient
import streetlight.web.io.OSMClient
import streetlight.web.io.TestApiClient
import streetlight.web.io.TestLocationClient
import streetlight.web.model.EventEditor
import streetlight.web.model.EventPostMode
import streetlight.web.model.EventScout
import streetlight.web.model.EventScoutStage
import streetlight.web.model.LocationEditor
import streetlight.web.model.LocationScout
import streetlight.web.model.MarkerMap
import streetlight.web.model.SiteConfig
import streetlight.web.model.Toaster
import kotlin.test.Test
import kotlin.test.assertEquals

class EventScoutTest: ViewTest() {

    private val location = testLocation()
    private val api = TestApiClient(location = TestLocationClient(listOf(location)))

    override fun api(): ApiClient = api

    @Test
    fun `posting another event here keeps the chosen location`() = runViewTest {
        val siteConfig = SiteConfig(scope)
        siteConfig.eventPostModeState.set(EventPostMode.ResetEvent)
        val toaster = Toaster(scope)
        val locationScout = LocationScout(
            galaxy = null,
            editor = LocationEditor(LocationEdit(), scope, api),
            scope = scope,
            osm = OSMClient(),
            map = MarkerMap(scope, GeoMap(scope, GeoCamera(scope))),
            toaster = toaster,
            api = api,
            siteConfig = siteConfig,
        )
        val editor = EventEditor(EventEdit(), scope, api)
        val scout = EventScout(null, editor, locationScout, scope, api, toaster, siteConfig)

        locationScout.selectionState.set(location)
        awaitUntil("the location to reach the event editor") { editor.editNow.locationId == location.locationId }
        scout.event.set(testEventLocation(location))

        scout.post()

        awaitUntil("the scout to return to the event search") { scout.stateNow.event == null }
        assertEquals(EventScoutStage.EventSearch, scout.stateNow.stage, "the scout should wait for another event")
        assertEquals(location, locationScout.stateNow.location, "the location should stay chosen")
        assertEquals(location.locationId, editor.editNow.locationId, "the next event should be at the same location")
    }
}
