# Package streetlight.server.daemon.crawler

## Introduction

The crawler, a standalone process that reads event feeds and the event pages they link to, and creates the events it finds.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.server.model` | `Server`, the DAO, event creation and the `MapReferenceClient` |
| `streetlight.server.db.datascope` | Location creation |
| `streetlight.server.daemon.agent` | Fetching, html parsing, the `SchemaMediator`, the `EntityClassifier`, schema validation and date parsing |

## Structure

`Crawler` holds what every piece of work shares: the DAO, its rooms and the run's state. It reads a few leads at once, each in its own coroutine, taking more as room opens, and hands each to `crawl`.

Shared work is paced or serialized so concurrent leads stay respectful and consistent: gates space the requests to each origin and to the map, the LM takes one request at a time, and database writes run one at a time through `Crawler.dbWrite`, each holding the reads it depends on.

A `Lead` is a page the crawler is given, with what is known of it before it is fetched: an `EventFeed`, or an `EventPage` carrying its feed and the `rawFeedEvent` the feed showed. `crawl` reads every lead: it checks the lead's last read did not stop it, fetches its `FetchDocument` in its origin's fetch mode, finds the schema its kind wants, fetches it again with scripting when the LM finds scripting required, and hands the document and schema to the service for its kind, pairing each kind of lead with its kind of schema. A lead whose last read stopped it, or that cannot be fetched or given a schema, passes on only what it carries: an event page delivers its `rawFeedEvent`. A lead that carries its page's `content`, such as a location or event lead sent with the html a user's browser rendered, is read from it in place of the first fetch, as a `Scripting` fetch served from its url, and does not wait on its robots gate.

Every lead is read by its schema, stored or asked of the LM. The values a page declares in its JSON-LD for its own event or place are laid over what was read, and win wherever they are given; a declared description is kept whole, whatever the source's `ParseMode`, even when it is a platform's shortened teaser, such as Meetup's, since Streetlight supplements such platforms rather than reproducing their content, and a declared ticket url becomes a `Tickets` link. Values a page gives only in its meta, such as its site name, do not outrank what was read. An event the page declares cancelled or postponed is `SchemaProblem.CalledOff`, found before the LM is asked, and its lead passes on nothing, not even its `rawFeedEvent`.

A page is known by the url it was served from, after redirects and normalized. Its link is recorded under that url, with the url asked for as an alias. A url the page declares for itself, such as a canonical link, is not used.

`crawlEventFeed` sends each event of its feed on: to its page as an `EventPage` lead, or straight to `deliverEvent` when it has no page worth reading. `crawlEventPage` delivers its event with the `rawFeedEvent` its lead carries. `deliverEvent` merges the two raw events, the page's preferred except its url and the start taken from the first pairing that parses, builds its edit with `RawEntity.toEventEdit`, and checks its title, start and location. It then classifies the edit with `EntityClassifier.classifyEvent`, adds its source or RSVP note, and creates the event. The note never reaches the embedding. A location's properties become a `LocationEdit` through `toLocationEdit`.

## Naming

A `RawEntity` is named `raw` followed by the entity its properties describe: `rawEvent`, `rawLocation`. A role goes between the two: `rawPageEvent`, `rawFeedEvent`, `rawDeclaredEvent`, `rawDeclaredLocation`. One that describes any entity is `rawEntity`.

## Crawler Model Analogy: Office Floorplan

The crawler is laid out as an office. `Crawler` is the office, with a main floor of desks and rooms off it.

| Part | Is |
|---|---|
| Front desk | `Crawler.checkLead`, where each lead arrives |
| Desk | A service function, an extension of `Crawler` |
| Package | A lead, or the data that travels between desks |
| Clipboard | The `ParseTracker`, which follows a lead along its path |
| Room | A class that owns an encapsulated body of work, such as `SchemaMediator` or `LocationSpawner` |
| Backroom | A class a room holds for part of its work, such as the `HtmlParserClient` behind `SchemaMediator` |

A lead arrives at the front desk as a package and travels from desk to desk until it reaches a dead end or is delivered to the database.

### Desks

A service function is named for its work, such as `crawlEventFeed` and `crawlEventPage`, and keeps no state of its own; a new kind of lead is read by a new one.

A service function reads as the steps of its work, each step a function of its own, such as `provisionFeedSchema` and `findEventElements`. A step records its own outcome on the tracker, and the service function only routes between steps. A step that serves more than one service, such as `deliverEvent`, lives in its own file.

Data that travels between steps together is carried as one package, named for its contents. Work flows one way: each desk passes its package on, and the last desk sends it to its destination. Events end in the database through `deliverEvent`, and the tracker ends in the link records and the report when the check is filed. A desk returns nothing. Work that branches on what a page turns out to hold stays with the desks.

### Rooms

A room owns its own state, never calls back into the desks, and is what a test replaces. It answers the desk that asks it with a result. A room that writes takes the `Crawler` as a context parameter for `dbWrite`.

| Room | Work |
|---|---|
| `PageFetcher` | Fetches each page once its origin's robots gate opens, spaced by the origin's `Crawl-delay` or one second when it gives none, with a plain request or in the browser, and keeps the gates and the browser. A plain request answered `429` or `503` is sent once more, after its `Retry-After` or 30 seconds |
| `SchemaMediator` | Finds each page's schema and keeps the LM's usage limit |
| `LocationSpawner` | Resolves event locations and keeps its map searches |
| `EntityClassifier` | Classifies what the crawler reads, by embeddings from its `EmbeddingsClient` |

## Feed Sources

Each feed is an `EventFeed`, read by the same `crawlEventFeed`. Its url arrives normalized from the DAO that builds it. `readCheckable` gives the leads not checked within a day. Leads are independent of one another, and their order carries no meaning.

| Source | Feed | Location of its events |
|---|---|---|
| `LocationEventFeed` | A location's events page | The location, unless an event names a distinct place |
| `GeneralEventFeed` | A page of local events at many locations, such as a newspaper's calendar | The place each event names; an event naming none is dropped |

Each source asks the LM with its own feed instructions. A `GeneralEventFeed` is a row of the `lead` table added by hand. A relative image a page declares in its meta tags or JSON-LD is resolved against the page when its lead is not `isExternalOrigin`, and dropped when it is. Each crawled event ends its description with the italic note "This information was automatically gathered, please check the source for updates.", the source linked to its page, or to its feed when it has none; a location's description read from its page ends with the same note. An event of a feed whose lead is `is_rsvp` carries instead an `RSVP` link to its page and the note "This event may require that you RSVP.", the RSVP linked. A publisher's robots.txt stands for its permission; a publisher without one is added only with its permission. It is always read `Partial`. A row of `LeadType.EventScan` is read the same way, once: `readCheckable` checks again only rows of `LeadType.EventFeed`. A scan without a name is labeled by its url.

The `lead` table holds every lead that is not a location's own feed, each with its `LeadType`, and for a lead a user submitted, the star who sent it and the galaxy it came from, for the follow-up. `toLead` builds the lead its type names. A general event feed is read again each day; a `LocationLead`, a url submitted as a location's page, is read once. A lead of the front desk is named in its tracker and report by its feed's name, or by its type and url.

## General Model

Parsing is built as a general model. A change is made only when its mechanism holds for any site, with no knowledge of a particular site, template or page builder, and when its need is seen across many origins. A site whose content does not fit the model is left unread rather than handled as a special case.

## Parse Reports

Reports are kept per build of the parse pipeline. `parserBuildId` names the build and is raised when any stage changes, so each build's results can be compared with the last. Paths are relative to the `daemon` working directory, and `../logs/parser` holds only build folders.

| Output | Path |
|---|---|
| Parse report | `../logs/parser/<build>/<type>-<address>.json`, named for the lead's type and url |
| Page html | `../logs/parser/<build>/html/<address>.html`, for each page that did not succeed |
| Prompt html | `../logs/parser/<build>/html/<address>-trim.html`, exactly as the LM read it, for each such page sent to the LM |

- `checkLead` builds a `ParseTracker` for each lead. When the check finishes, a report is written only if `needsReport()`: some page needs work, as stated under Links, the lead was read and yielded no record created, duplicated or known, a location was spawned or failed to be, an event was classified, or the check failed. A later check of the same lead in the same build overwrites its report, and the report saves the lead's html.
- An exception during a check is caught and reported as the check's `failure`, and its link records are kept. The lead's `checked_at` stays set, so a lead that keeps failing waits for its next turn.
- A step that records takes its tracker as an argument, never null and never inside a package, reports raw objects to it (the fetch and document, schemas tried or created, and its outcome at every exit), and consults it before fetching. A step computes no reported value.
- The tracker derives every reported value. A new stat is added in the tracker alone.
- A check has one `ParseTracker`, passed as a context. It tracks each page of the check by its url, the lead's own page and any page the lead leads to, and every tracking call names the url of the page it records.
- `collect(url, item)` keeps any other object the tracker draws on when the report is built, such as the feed's event elements.
- `tracker.page(url)` is the `SchemaObserver` passed to the `SchemaMediator`, which fills the page's schema and LM stages. Each request to the LM is its own entry in the page's `lm` list, of kind `schema`, `time` or `description`.
- A report holds the `lead`'s own page, each page it led to, and the counts of the records it found. Each page has one section per stage, as far as it got: `fetch`, `trim`, `lm`, `schema`, and its `outcome` and HTTP `status`. A page's `notes` hold only what its fields do not already say: a record's trouble is noted once, on its own page or else on the lead's, and a fetch problem is noted only when it has no status.

| Stage | Logged |
|---|---|
| Fetch | HTTP status, mode, final url, chars, visible text chars, time taken |
| Trim | The page's trim stats, once for every request made of it |
| LM | Model, chars cut by the cap, attempts, tokens, time taken, raw response |
| Schema | Stored or new, stored schemas tried, validation result, fields dropped; for a feed, event count and matches per field |
| Classification | Each event's `EventClassification`, on the page it was read from: its most similar tags with their similarities, and the tags given |
| Records | Counts of the records found, created, past, unnamed, shortened, duplicate, known and failed, of locations spawned and failed, and of events classified. What became of each record is a note on the page it was read from, or on the lead's page |

A page's report carries its `state` (`attempted`, `skipped`, `benched`, `deferred`) and the values recorded on its link.

## Links

A page that reaches its server records what its read found on its `Link`, the feed included. `checkLocation` records them through `recordLink` when the check finishes, creating the link when missing. A page that never reached its server leaves no record: a 5xx or no response, a benched or deferred page, or one skipped by its link.

| Column | Values | Set from |
|---|---|---|
| `access` | `Granted`, `RobotsBlock`, `Refused`, never null | The HTTP status and robots.txt. Any status other than 200 below 500 is `Refused` |
| `content` | `Schema`, `OffSchema`, `OffScope`, `Unknown`, `Unread` | What the read found. `OffSchema` is the LM's "not the expected content", `Unknown` is not html, `Unread` needs scripting. Null when not yet classified |
| `schemaType` | `EventFeed`, `EventPage` | The schema that read it, when `content` is `Schema` |
| `parseOutcome` | `Complete`, `Partial`, `Fail` | The parse with the schema. A rejected schema, or a feed whose event selector matched nothing, is `Fail`. Content still unread under scripting is `Fail` |

- Each read overwrites the link's values, since they describe the last read.
- An event whose link normalizes to the feed's own url has no page of its own, such as a link to a route inside the feed's app, and is built from the feed's data.
- An event page is read at most once in a check. An event whose page was already read in the check is built from its own feed data, since one page cannot speak for each event's date.
- An event whose page link already has an outcome is dropped as known, and counted under `known`.
- An event page is read again only when its link is `Granted` with no `parseOutcome` and content that is null or `Unread`: a read interrupted before classification, or content awaiting scripting. `wantsRead` states it.
- A feed is not read again when its link is not `Granted`, its content is `OffSchema`, `OffScope` or `Unknown`, or its `parseOutcome` is `Fail`. `stopsFeed` states it. A failure waits for a better build. A lead that carries its page's `content` is read whatever its link holds.
- An event whose date text does not parse marks the lead `Partial`, and the page it was read from `Partial` with a note.
- A report is written, and html saved, for each check where some page is `Partial` or `Fail`, or has content other than `Schema`.

## Parse Limit

A check reads at most 30 event pages. A page past the limit is recorded as `deferred`, gets no link, and its event is dropped for this check, so the next check reads the page and builds the event in full. Pages skipped by their link do not count toward the limit, so a large feed is read in full over a series of checks.

## Fetch Mode

An origin is fetched in `Basic` mode until the LM reports a page of it as missing its content because its javascript has not run, whether or not the content was the expected kind. `FetchDocument` carries its fetch mode, and only a `Basic` fetch is asked. `crawl` then fetches that page again through the `PageFetcher`'s browser within the same read, and `registerScriptingRequired` moves the origin to `Scripting` only when that fetch yields a schema. Its later pages are then fetched through Playwright too.

A `Scripting` fetch blocks images, media and fonts, never stylesheets. It scrolls down with the mouse wheel after the load event until the page's text, its frames included, has held steady for 3 seconds, up to 15 seconds, so content rendered late or loaded on scroll is included. The wheel scrolls whatever container is under the pointer, which a script scrolling the window does not reach. It then folds each iframe holding text into the page as a `div` marked `data-frame-src`, with its relative links made absolute, since the page's html leaves out iframe contents.

## Events

A feed's events are created only with a title, and a start date and time that is still ahead. This is the final guard on what reaches users. An event with no title is dropped and counted under `unnamed`, an event whose start cannot be parsed from its date and time text is dropped and its text noted, and an event whose start has passed is dropped and counted under `past`. An untitled or unparsed event marks its feed and page `Partial`. Feeds supply what they readily can; other events are posted by hand.

An event's title has its bracketed notes cut, such as "[SOLD OUT]". An event is a duplicate, and is not created, when an event at the location on the same local day has a title that `fuzzyMatches` its own, whether posted by the crawler or by a person and whatever its start time. Each duplicate is counted, and its pair of titles noted on its page. An event whose image cannot be stored is still created, without the image, and noted on its page, since the image is decoration and the event is the data.

A source's `ParseMode` sets how much is read: `None` leaves it out of `readCheckable`, `Partial` shortens each description it reads, and `Full` keeps each description whole. A declared description is never shortened. An event's shortened description is kept to 1,000 characters of markdown, respecting the venue's own writing. `shortenDescription` keeps the whole paragraphs that fit, or the first sentences when the first paragraph does not, and ends a shortened description with a `Read more` link to the event page. Shortened descriptions are counted under `shortened`.

An event's date text joins its `date`, `month` and `day` selectors. When an event is read from both its feed and its page, its date and start time are taken as the first pair that parses: the page's own, the feed's date with the page's time, the page's date with the feed's time, then the feed's own.

Date text is parsed by `parseLocalDateTime`, after every Unicode space is folded to a plain one. It reads a year-less date as the nearest such date to today, using a named weekday to choose among candidates.

## Locations

An event is placed at its feed's location unless its location text clearly names a distinct place. The text is the page's `location`, or else the feed's `eventLocation`. `LocationSpawner` takes a place as distinct only when all of these hold:

- the text does not `fuzzyMatches` the feed location's name;
- OpenStreetMap finds a single place somewhere a person can go (see Places on the Map) whose name `fuzzyMatches` the text, within 200 km of the feed;
- no such place lies within 150 m of the feed location, which would make the text a room or stage of the feed's venue.

Among several places, the closest name wins, then the nearest place. A distinct place already stored, by its map id or by a matching name within 150 m, is used as is; otherwise it is created from the map with no caller, so it has no edit log and no review. A location the crawler creates whose image cannot be stored is created without it. When nothing holds, the event stays at the feed's location: a feed lists its own venue far more often than another, and a venue's rooms seldom appear on the map. A general feed has no location of its own, so the 150 m rule does not apply. An event of a general feed that gives an address is placed as an event lead's is, by `LocationSpawner.placeEvent` (see Event Leads), keeping only places within 200 km of the feed; one that gives none is searched by its location text as above. An event whose place is not found is dropped and noted on its page, marking its feed and page `Partial`.

Map searches are bounded to the 200 km around the feed's point, paced to one every 2 seconds across all of the crawler's searches by the `OSMGate` the crawler gives its `LocationSpawner`, each search reserving the next slot so concurrent searches stay spaced, and kept for the crawler's run behind a mutex. Each location resolved, matched, fallen back or failed is noted on its event's page, and a check that spawned a location, or failed to, is always reported.

## Places on the Map

A map result is a place when it is somewhere a person can go, such as a business, building, park or preserve. A result of category `place` (a city, town or neighborhood), `highway` (a road) or an administrative `boundary` is never taken. An address search takes a result whose house number and road match, whatever its kind.

A place's name is cleaned by `cleanPlaceName` before it is searched: notes in parentheses or brackets are dropped, as are phrases that name no place, such as "parking lot". A city, town or village in a map address is taken as the location's city.

## Location Leads

A location lead is read once, so it is read directly: the LM fills a `LocationSchema` with the location's details rather than selectors, through `SchemaMediator.readLocation`, and nothing is stored for reuse. `crawlLocationLead` turns the details into a `RawEntity`, the page's meta image first. A location already stored at its address, whose name matches, is a duplicate. `deliverLocation` places it on OpenStreetMap with free-form searches: its address with its city, state and postal code first, then its name with them. Of the places whose name `fuzzyMatches`, one whose website shares the lead's origin wins first, then the closest name. When the name finds no place, the name the page declares for itself (`og:site_name`) is searched once more, and a place it finds gives the location that name. A place already stored by its map id is a duplicate. Otherwise the location is created with no caller from the page's details, the map filling what the page lacks and giving the address. When no name finds a place, a building-level place at its address is enough: a location stored there by map id or matching name is a duplicate, and otherwise the location is created there with the page's details and no map id, since the map names something else there or nothing. A lead whose name, place or required fields are missing is noted as failed and not created.

A location with a website, no host and no `checked_at` is read once as a location lead carrying its `locationId`, after the locations' feeds and the stored leads. `location.checked_at` marks that read, and `checked_feed_at` marks the reads of its feed. `mergeAndUpdateLocation` fills only the empty fields of the location from the page (`mergeLeft`), with no map search and no caller. A merge that changes nothing is a duplicate.

An address is searched by its street alone, the part before its first comma, with the area added from the city, state and postal code. A unit in an address, such as `#100`, `Unit 148`, `Ste 1400`, `Suite D` or `Building 7`, is left out of every map search, which finds nothing with it, and is added back to the address the map gives a created location.

A location created at an address, with no map id, takes only the address from the map: its street, city, state, country and point. Details of the place the map names there, such as its type, hours, rank and website, are left out.

## Event Leads

An event lead is read directly as well: the LM fills an `EventSchema` with the event's details, its dates as an ISO date and 24-hour times, and the details of the place it happens at, through `SchemaMediator.readEvent`. `crawlEventLead` places the event first. A location already stored at the place's address, whose name matches, is used without the map. Otherwise `LocationSpawner.findPlace` searches the map by the place's address with its city, state and postal code, then by its name with them, then by its name with its state alone, and by its name alone when a feed's point fences the search. It keeps a place whose name matches, preferring one on the origin of the website the page gives for it, then one on the road of its address. A place already stored, by its map id or by a matching name at its point, is used as it is; otherwise the location is created first from the map, with the page's website preferred to the map's. The event is then created at it through `createEventAt`, the core `deliverEvent` also ends in, and is read `Partial`. When no place on the map matches the place's name, its address alone is taken when the map knows it: a building-level hit with the address's house number on a road holding the longest word of its street, searched with its city, state and postal code, then with its state alone. The event goes to a location already stored at that point with no name of its own, or one created there with no name and no map id. A location needs only a point. An event whose place and address both find nothing is noted as unlocated.

`LocationSpawner.placeEvent` holds this placing, shared by event leads and the events of general feeds. `findPlace`, `readStoredLocation` and `createLocationFrom` serve location leads too.

The place of an event its JSON-LD declares is its first `location` that is not a `VirtualLocation`. Its city, state and postal code travel as the event's `Area`, and its state also as its `Region`.

## Strikes

`ParseTracker` counts consecutive strikes per origin within one check. A 4xx or 5xx response to a page adds a strike to the origin of the url requested, and a successful fetch from that origin clears them. A page schema that fails validation adds a schema strike to its origin, and a page read in full clears them, so the LM is not asked again and again for an origin whose pages it cannot read. At 3 strikes of either kind, `shouldFetch` is false, and `crawlEventFeed` records the origin's remaining event pages as `benched`, whose events keep the data read from the feed.

## Workflows

Starting a new build:

1. Raise `parserBuildId`.
2. Dump the `parser` table to `logs/schema/parser-<timestamp>.json`.
3. Delete all rows of `parser`, `link` and `event`, clear `location.checked_feed_at`, and reset each `origin` to `fetch_mode` Basic with no `robots_txt`, so the build reads every location from a fresh state.
