# Package streetlight.server.daemon

## Introduction

A standalone process that reads event feeds and the event pages they link to, and creates the events it finds.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.server.model` | `Server`, the DAO, event creation and the `MapReferenceClient` |
| `streetlight.server.db.datascope` | Location creation |
| `streetlight.agent` | Fetching, html parsing, the `SchemaMediator`, schema validation and date parsing |

## Feed Sources

Each feed is an `EventFeedSource`, read by the same `EventFeedReader`. `readCheckable` gives the feeds not checked within a day: the locations' own first, so their venues are stored before a general feed names them, then the general feeds.

| Source | Feed | Location of its events |
|---|---|---|
| `LocationConfigContent` | A location's events page | The location, unless an event names a distinct place |
| `EventFeed` | A page of local events at many locations, such as a newspaper's calendar | The place each event names; an event naming none is dropped |

Each source asks the LM with its own feed instructions. An `EventFeed` row is added by hand, with the publisher's permission, and is always read `Partial`.

## General Model

Parsing is built as a general model. A change is made only when its mechanism holds for any site, with no knowledge of a particular site, template or page builder, and when its need is seen across many origins. A site whose content does not fit the model is left unread rather than handled as a special case.

## Parse Reports

Reports are kept per build of the parse pipeline. `parserBuildId` names the build and is raised when any stage changes, so each build's results can be compared with the last. Paths are relative to the `daemon` working directory, and `../logs/parser` holds only build folders.

| Output | Path |
|---|---|
| Parse report | `../logs/parser/<build>/<origin>.json` |
| Page html | `../logs/parser/<build>/html/<address>.html`, for each page that did not succeed |
| Prompt html | `../logs/parser/<build>/html/<address>-trim.html`, exactly as the LM read it, for each such page sent to the LM |

- `checkLocation` builds a `ParseTracker` for each location and reports the event stage to it. When the check finishes, a report is written only if `needsReport()`: some page needs work, as stated under Links, the feed was read and yielded no event created, duplicated or known, or the check failed. A later check in the same build overwrites the report, and the report saves the feed's html.
- An exception during a check is caught and reported as the check's `failure`, its link records are kept, and the daemon moves to the next location. The location's `checked_at` stays set, so a location that keeps failing waits for its next turn.
- A reader takes its tracker, never null, reports raw objects to it (the fetch and document, schemas tried or created, and its outcome at every exit of `read()`), and consults it before fetching. A reader computes no reported value.
- The tracker derives every reported value. A new stat is added in the tracker alone.
- The feed reader hands each event page reader the child `PageTracker` from `tracker.page(url)`.
- `PageTracker.collect` keeps any other object the tracker draws on when the report is built, such as the feed's event elements.
- A `PageTracker` is the `SchemaObserver` passed to the `SchemaMediator`, which fills the schema and LM stages. Each request to the LM is its own entry in the page's `lm` list, of kind `schema` or `time`.
- A report holds the feed page, each event page read, and the events the feed yielded. Each page has one section per stage, as far as it got: `fetch`, `lm`, `schema`, and its `outcome` and HTTP `status`.

| Stage | Logged |
|---|---|
| Fetch | HTTP status, mode, final url, chars, visible text chars, time taken |
| LM | Model, trim stats, chars cut by the cap, attempts, tokens, time taken, raw response |
| Schema | Stored or new, stored schemas tried, validation result, fields dropped; for a feed, event count and matches per field |
| Events | Found, created, past, untitled, shortened, duplicates with their titles, failed creates with their reasons, date text that did not parse, events with no location |

A page's report carries its `state` (`attempted`, `skipped`, `benched`, `deferred`) and the values recorded on its link.

## Links

A page that reaches its server records what its read found on its `Link`, the feed included. `checkLocation` records them through `recordLink` when the check finishes, creating the link when missing. A page that never reached its server leaves no record: a 5xx or no response, a benched or deferred page, or one skipped by its link.

| Column | Values | Set from |
|---|---|---|
| `access` | `Granted`, `RobotsBlock`, `Refused`, never null | The HTTP status and robots.txt. Any status other than 200 below 500 is `Refused` |
| `content` | `Schema`, `OffSchema`, `OffScope`, `Unknown`, `Unread` | What the read found. `OffSchema` is the LM's "not the expected content", `Unknown` is not html, `Unread` needs scripting. Null when not yet classified |
| `schemaType` | `EventFeed`, `EventPage` | The schema that read it, when `content` is `Schema` |
| `parseOutcome` | `Complete`, `Partial`, `Fail` | The parse with the schema. A rejected schema, or a feed whose event selector matched nothing, is `Fail`. Content still unread under scripting is `Fail` |
| `parseNote` | Text | Each reason behind the values, joined |

- Each read overwrites the link's values, since they describe the last read.
- An event whose link normalizes to the feed's own url has no page of its own, such as a link to a route inside the feed's app, and is built from the feed's data.
- An event page is read at most once in a check. An event whose page was already read in the check is built from its own feed data, since one page cannot speak for each event's date.
- An event whose page link already has an outcome is dropped as known, and counted under `known`.
- An event page is read again only when its link is `Granted` with no `parseOutcome` and content that is null or `Unread`: a read interrupted before classification, or content awaiting scripting. `wantsRead` states it.
- A feed is not read again when its link is not `Granted`, its content is `OffSchema`, `OffScope` or `Unknown`, or its `parseOutcome` is `Fail`. `stopsFeed` states it. A failure waits for a better build.
- An event whose date text does not parse marks the feed, and the page it was read from, `Partial`.
- A report is written, and html saved, for each check where some page is `Partial` or `Fail`, or has content other than `Schema`.

## Parse Limit

A check reads at most 30 event pages. A page past the limit is recorded as `deferred`, gets no link, and its event is dropped for this check, so the next check reads the page and builds the event in full. Pages skipped by their link do not count toward the limit, so a large feed is read in full over a series of checks.

## Fetch Mode

An origin is fetched in `Basic` mode until the LM reports a page of it as incomplete content, whether or not the content was the expected kind. `registerIncomplete` then moves the origin to `Scripting`, and the reader fetches that page again through Playwright within the same read. Its later pages are fetched through Playwright too.

A `Scripting` fetch scrolls down with the mouse wheel after the load event until the page's text, its frames included, has held steady for 3 seconds, up to 15 seconds, so content rendered late or loaded on scroll is included. The wheel scrolls whatever container is under the pointer, which a script scrolling the window does not reach. It then folds each iframe holding text into the page as a `div` marked `data-frame-src`, with its relative links made absolute, since the page's html leaves out iframe contents.

## Events

A feed's events are created only with a title, and a start date and time that is still ahead. This is the final guard on what reaches users. An event with no title is dropped and counted under `untitled`, an event whose start cannot be parsed from its date and time text is dropped and its text reported under `unparsedDates`, and an event whose start has passed is dropped and counted under `past`. An untitled or unparsed event marks its feed and page `Partial`. Feeds supply what they readily can; other events are posted by hand.

An event's title has its bracketed notes cut, such as "[SOLD OUT]". An event is a duplicate, and is not created, when an event at the location on the same local day has a title that `fuzzyMatches` its own, whether posted by the daemon or by a person and whatever its start time. Each duplicate is counted, and its pair of titles reported under `duplicateTitles`. An event whose image cannot be stored is still created, without the image, and reported under `imageFailures`, since the image is decoration and the event is the data.

A source's `ParseMode` sets how much is read: `None` skips it, `Partial` shortens each description, and `Full` keeps each description whole. An event's shortened description is kept to 1,000 characters of markdown, respecting the venue's own writing. `shortenDescription` keeps the whole paragraphs that fit, or the first sentences when the first paragraph does not, and ends a shortened description with a `Read more` link to the event page. Shortened descriptions are counted under `shortened`.

An event's date text joins its `date`, `month` and `day` selectors. When an event is read from both its feed and its page, its date and start time are taken as the first pair that parses: the page's own, the feed's date with the page's time, the page's date with the feed's time, then the feed's own.

Date text is parsed by `parseLocalDateTime`, after every Unicode space is folded to a plain one. It reads a year-less date as the nearest such date to today, using a named weekday to choose among candidates.

## Locations

An event is placed at its feed's location unless its location text clearly names a distinct place. The text is the page's `location`, or else the feed's `eventLocation`. `LocationSpawner` takes a place as distinct only when all of these hold:

- the text does not `fuzzyMatches` the feed location's name;
- OpenStreetMap finds a single place (place rank 30, not a city or street) whose name `fuzzyMatches` the text, within 200 km of the feed;
- no such place lies within 150 m of the feed location, which would make the text a room or stage of the feed's venue.

Among several places, the closest name wins, then the nearest place. A distinct place already stored, by its map id or by a matching name within 150 m, is used as is; otherwise it is created from the map with no caller, so it has no edit log and no review. When nothing holds, the event stays at the feed's location: a feed lists its own venue far more often than another, and a venue's rooms seldom appear on the map. A general feed has no location of its own, so the 150 m rule does not apply, and an event whose place is not found is dropped and reported under `unlocatedEvents`, marking its feed and page `Partial`.

Map searches are bounded to the 200 km around the feed's point, paced to one a second, and kept for the daemon's run. The report lists `spawnedLocations`, `matchedLocations`, `fallbackLocations` and `locationFailures`, and a check that spawned a location, or failed to, is always reported.

## Strikes

`ParseTracker` counts consecutive strikes per origin within one check. A 4xx or 5xx response to a page adds a strike to the origin of the url requested, and a successful fetch from that origin clears them. At 3 strikes, `shouldFetch` is false, and the feed reader records the origin's remaining event pages as `benched`, whose events keep the data read from the feed.

## Workflows

Starting a new build:

1. Raise `parserBuildId`.
2. Dump the `parser` table to `logs/schema/parser-<timestamp>.json`.
3. Delete all rows of `parser`, `link` and `event`, clear `location.checked_at`, and reset each `origin` to `fetch_mode` Basic with no `robots_txt`, so the build reads every location from a fresh state.
