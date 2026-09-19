package streetlight.web.io

import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
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
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*
import streetlight.model.writeCursor
import web.sockets.WebSocket
import web.sse.EventSource
import kotlin.uuid.Uuid

interface EventClient {
    suspend fun readEventId(eventId: EventId): Outcome<Event>
    suspend fun readEventSlug(slug: Slug): Outcome<EventLocation>
    suspend fun readEventFeed(): Outcome<List<Event>>
    suspend fun createEvent(event: EventEdit): Outcome<Event>
    suspend fun updateEvent(event: EventEdit): Outcome<Event>
    suspend fun readEventUpdaterContent(slug: Slug): Outcome<EventUpdaterContent>
    suspend fun parseMultiEvent(request: ParseRequest): Outcome<MultiEventParseResponse>
    suspend fun parseSingleEvent(request: ParseRequest): Outcome<EventEdit>
    suspend fun readLocationEvents(slug: Slug): Outcome<List<Event>>
    suspend fun readEventLocations(eventIds: List<EventId>): Outcome<List<EventLocation>>
    suspend fun readEventStars(): Outcome<List<EventId>>
    suspend fun queryMap(request: MapQueryLegacy): Outcome<List<EventLocation>>
}

class BrowserEventClient(private val client: FetchClient): EventClient {
    override suspend fun readEventId(eventId: EventId) = client.getApi(Api.Events.ReadId, eventId)
    override suspend fun readEventSlug(slug: Slug) = client.getApi(Api.Events.ReadSlug, slug)
    override suspend fun readEventFeed() = client.getApi(Api.Events)
    override suspend fun createEvent(event: EventEdit) = client.postApi(Api.Events.CreateEvent, event)
    override suspend fun updateEvent(event: EventEdit) = client.postApi(Api.Events.UpdateEvent, event)
    override suspend fun readEventUpdaterContent(slug: Slug) = client.getApi(Api.Events.ReadUpdaterContent, slug)
    override suspend fun parseMultiEvent(request: ParseRequest) = client.postApi(Api.Events.ParseMultiEvents, request)
    override suspend fun parseSingleEvent(request: ParseRequest) = client.postApi(Api.Events.ParseSingleEvent, request)
    override suspend fun readLocationEvents(slug: Slug) = client.getApi(Api.Events.AtLocation, slug)
    override suspend fun readEventLocations(eventIds: List<EventId>) = client.postApi(Api.Events.ReadEventLocations, eventIds)
    override suspend fun readEventStars() = client.getApi(Api.Events.ReadLights)
    override suspend fun queryMap(request: MapQueryLegacy) = client.getApi(Api.Events.QueryMap, request.toQuery())
}
