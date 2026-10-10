# Feed Unification

One `feedSection` serves every page that shows a feed. Each page is a feed context, and each context offers a set of feed types the viewer switches between with an icon menu left of the heading, as the `FeedMode` switch sits to its right. The chosen type travels as a query parameter of the route, so the server renders the right feed on the first load and a link keeps it.

## Shape

| Part | Holds |
|---|---|
| `FeedContext` | Where the feed is shown, a sealed interface carrying the id its queries need: `Home`, `City(cityId)`, `Location(locationId)`, `Galaxy(galaxyId)`, `Star(username)` |
| `FeedType` | What the feed lists, an enum: `Events`, `Locations`, `Media`, `GalaxyPosts`, `Posts` |
| `FeedContext.types` | The types a context offers |
| `FeedSource(context, type)` | Carried by every page of a feed as `EntityFeed.source`, so its section, its menu and its more button know where the next page is read. It replaces the old `FeedSource` enum, and the `galaxyId` and `cityId` parameters of `feedSection` |
| `FeedRequest(source, cursor)` | The body of `Api.Feeds.Read` |

`feedSection(feed)` takes the feed alone. The section reads the context and type from it, sets them on itself for the browser, and builds the type menu from `context.types`. A context with one type shows no menu.

### Contexts

| Context | Types | Notes |
|---|---|---|
| `Home` | `Events`, `Media`, `GalaxyPosts` | `GalaxyPosts` is offered to a signed-in star only: the posts of the galaxies the star follows (`GalaxyStarTable`). `Events` is the upcoming feed as it stands. `Media` is not yet served anywhere |
| `City(cityId)` | `Events`, `Locations` | `Locations` lists the city's locations, newest first (`createdAt` descending) to begin |
| `Location(locationId)` | `Events` | The upcoming events at the location, for its page (`Events.AtLocation` returns a list today). More types may follow |
| `Galaxy(galaxyId)` | `Posts` | One type for now. Separate feeds of its posted events, locations and media are possible later |
| `Star(starId)` | `Media` | The star's own content |

## Progress

1. ✅ **Types** (2026-10-10): `FeedContext`, `FeedType`, and `FeedSource` as the pair of them, on `EntityFeed.source`, nullable for the map's feed. `feedSection(feed)` takes the feed alone. The section carries its source as the JSON attribute `AppAttribute.FeedSource`; `AppAttribute.GalaxyId` and `CityId` are gone. Each context lists only the types served so far: `Home` (`Events`, `GalaxyPosts`), `City` (`Events`), `Galaxy` (`Posts`), `Star` (`Media`). Behavior unchanged. Not yet compiled.
2. ✅ **One read** (2026-10-10): `Api.Feeds.Read`, a `PostEndpoint<FeedRequest, EntityFeed>`, served by `serveFeeds` from `DaoScope.readFeed`, a `when` over the context and type, refusing a type the context does not offer. `FeedClient` serves it, and the more button, sort by mark, tag filter, search and the Earth galaxy map read through it. `Events.ReadFeed`, `Cities.ReadFeed` and `Posts.ReadFeed` are retired, with the cursor endpoint interfaces and readers they alone used (`CursorEndpoint.kt`, the cursor readers in `CursorEndpointUtility.kt`). `EventFeedApiTest` reads through the new endpoint and checks the refusal. Not yet compiled.
3. ✅ **The menu** (2026-10-10): `feedSection(feed, typeRoute)` shows the types offered left of the heading, which is the type's name; the shown type in `PrimaryFg`, each a link through `typeRoute` when the caller gives one. Icons: `Events` `Calendar`, `GalaxyPosts` `Planet`, `Posts` `News`, `Media` `Photo`. A single type still shows. `EntityFeed.types` carries the types offered this viewer: a visitor's home offers `Events` alone. `HomeRoute(feed)` is a `FeedRoute`, writing `?feed=<Type>` for a chosen type, read by `StaticParse`, whose block now has the query `Parameters` as its receiver (as does `SlugParse`). `Api.Content.Home` takes `feed`, and `renderHome` reads it from the query. The route dock matches a feed route by its `defaultFeedRoute`. Tests: `FeedRouteTest` (route round trip), and the home type choice in `UpcomingEventFeedTest`. Not yet compiled.
4. **New types**
   * ✅ City `Locations` (2026-10-10): `readCityLocations` pages a city's locations newest first by `createdAt` then id, through the shared `afterCursor`. `CityRoute(slug, feed)` is a `FeedRoute`, read by `SlugParse`; `Cities.ReadContent` takes `feed`, and `renderCity` reads it. The route dock normalizes a feed route to its `defaultFeedRoute` (`dockRoute`) for its state, merges and marking. Icon `MapPin`. Tests in `CityFeedTest` and `FeedRouteTest`.
   * ✅ Home `Media` and Star `Media` in pages (2026-10-10): `readNewestMedia` pages media newest first, a star's alone when named, behind `readMediaFeed(context)`. Home offers `Events`, `Media`, `GalaxyPosts`; a visitor all but `GalaxyPosts`. `readMedia(username, callerId)` is gone. Tests in `MediaFeedTest`.
   * ✅ Location `Events` (2026-10-10): `LocationContent.feed` replaces its list of events, read by `readLocationEventFeed` from 6 hours ago (`LocationEventGrace`), through `readUpcomingEvents`, which gained `locationId` and `since`. The location layout's events block renders a `feedSection`, filters included. `Entity.toCells(context)` leaves out an event's location in a location's own feed; the context travels as a parameter from the section and from `appendFeed`, since a context parameter on `feedRow` would reach every caller. Test in `LocationEventFeedTest`.

5. ✅ **Feeds within the view** (2026-10-10): `AppRoute.screenKey` replaces `retainWithinScreen`: Portal keeps the view across routes with one key, and rebuilds on a refresh. A `FeedRoute`'s key is its `defaultFeedRoute`; every `EarthRoute`'s is `Screen.Earth`. `followFeedRoute` in `viewHome` and `viewCity` reads the content again for the new feed and swaps the feed section in place, Home refreshing its map markers. `toHttpProblem` maps 400 to `HttpProblem.BadRequest`. Compiled; feed tests and `FeedRouteTest` pass.

## Decisions

* **A POST body.** The request travels as a JSON body, so the sealed context and the cursor of any type cross the wire as they are, with no parameter per field.
* **Star by username.** `Star` carries no id in its DTO; its username is its public identity, and its media are read by it.
* **`FeedType`.** Named to the codebase's `FooType` pattern, renamed from `FeedKind` (2026-10-10).
* **Types as they are served.** A context lists a type once the server reads it, so the menu never offers an empty feed.
* **Events read for the caller.** An `Events` feed is now read as seen by the caller, as the city feed already was.

## Proposals

* **Cursor by type.** Each type pages by its own `EntityCursor`: upcoming events by start, locations and media by `createdAt` descending, galaxy posts by time or mark score.
* **Filters by type.** The tag filter and search belong to `Events`. `Locations` may take search; the rest take neither until decided.
* **Route parameter.** Each route that shows a feed takes an optional `feed` query parameter naming the type, absent for the default. The menu changes it by navigating, so the backstack holds each choice.

## Decided Elsewhere

* **Star:** the events a star hosts join the `Star` context as a type later.
* **Starred:** a star's starred content is not a feed type. Starred events get a calendar page, and starred locations an Earth route, each clearer for its purpose than a feed.
* **Map:** the map never holds a feed section; it is the alternative to the feed. It shares the feed's structures, as `Posts.ReadMapQuery` returns an `EntityFeed`, because they suit it.

## Answered

1. The type menu shows for a single type, telling the viewer what the feed holds.
2. A visitor is not offered `GalaxyPosts`; the switch is not overloaded with a sign-in prompt.
3. The heading is the type's name.
4. A route takes `feed` once its context offers a second type; Home first, City with `Locations`.
