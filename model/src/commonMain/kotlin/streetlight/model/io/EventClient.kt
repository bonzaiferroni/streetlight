package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.AccountUpgradeRequest
import kampfire.model.EmailChange
import kampfire.model.GeoPoint
import kampfire.model.GeoRect
import kampfire.model.LoginRequest
import kampfire.model.Outcome
import kampfire.model.PasswordChange
import kampfire.model.PasswordVerification
import kampfire.model.SignUpRequest
import kampfire.model.Url
import koala.Image
import koala.model.DocId
import koala.model.DocTableItem
import kotlin.uuid.Uuid
import streetlight.model.Api
import streetlight.model.data.*

/** The calls of `Api.Events`. */
class EventClient(private val transport: ApiTransport) {
    suspend fun readEventId(eventId: EventId): Outcome<Event> = transport.getApi(Api.Events.ReadId, eventId)
    suspend fun readEventSlug(slug: Slug): Outcome<EventLocation> = transport.getApi(Api.Events.ReadSlug, slug)
    suspend fun readEventFeed(): Outcome<List<Event>> = transport.getApi(Api.Events)
    suspend fun createEvent(event: EventEdit): Outcome<Event> = transport.postApi(Api.Events.CreateEvent, event)
    suspend fun updateEvent(event: EventEdit): Outcome<Event> = transport.postApi(Api.Events.UpdateEvent, event)
    suspend fun readEventUpdaterContent(slug: Slug): Outcome<EventUpdaterContent> = transport.getApi(Api.Events.ReadUpdaterContent, slug)
    suspend fun parseMultiEvent(request: ParseRequest): Outcome<MultiEventParseResponse> = transport.postApi(Api.Events.ParseMultiEvents, request)
    suspend fun parseSingleEvent(request: ParseRequest): Outcome<EventEdit> = transport.postApi(Api.Events.ParseSingleEvent, request)
    suspend fun readLocationEvents(slug: Slug): Outcome<List<Event>> = transport.getApi(Api.Events.AtLocation, slug)
    suspend fun readEventLocations(eventIds: List<EventId>): Outcome<List<EventLocation>> = transport.postApi(Api.Events.ReadEventLocations, eventIds)
    suspend fun readEventStars(): Outcome<List<EventId>> = transport.getApi(Api.Events.ReadLights)
    suspend fun queryMap(request: MapQueryLegacy): Outcome<List<EventLocation>> = transport.getApi(Api.Events.QueryMap, request.toQuery())
}
