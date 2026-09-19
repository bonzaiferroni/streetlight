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

class TestEventClient: EventClient {
    override suspend fun readEventId(eventId: EventId): Outcome<Event> = TODO()
    override suspend fun readEventSlug(slug: Slug): Outcome<EventLocation> = TODO()
    override suspend fun readEventFeed(): Outcome<List<Event>> = TODO()
    override suspend fun createEvent(event: EventEdit): Outcome<Event> = TODO()
    override suspend fun updateEvent(event: EventEdit): Outcome<Event> = TODO()
    override suspend fun readEventUpdaterContent(slug: Slug): Outcome<EventUpdaterContent> = TODO()
    override suspend fun parseMultiEvent(request: ParseRequest): Outcome<MultiEventParseResponse> = TODO()
    override suspend fun parseSingleEvent(request: ParseRequest): Outcome<EventEdit> = TODO()
    override suspend fun readLocationEvents(slug: Slug): Outcome<List<Event>> = TODO()
    override suspend fun readEventLocations(eventIds: List<EventId>): Outcome<List<EventLocation>> = TODO()
    override suspend fun readEventStars(): Outcome<List<EventId>> = TODO()
    override suspend fun queryMap(request: MapQueryLegacy): Outcome<List<EventLocation>> = TODO()
}
