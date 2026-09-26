# Package streetlight.server.daemon

## Introduction

A standalone process that reads location event feeds and the event pages they link to, and creates the events it finds.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.server.model` | `Server`, the DAO and event creation |
| `streetlight.agent` | Fetching, html parsing and the LM schema parser |

## Parse Reports

Reports are kept per build of the parse pipeline. `parserBuildId` names the build and is raised when any stage changes, so each build's results can be compared with the last. Paths are relative to the `daemon` working directory, and `../logs/parser` holds only build folders.

| Output | Path |
|---|---|
| Parse report | `../logs/parser/<build>/<origin>.json` |
| Page html | `../logs/parser/<build>/html/<address>.html`, for each page that did not succeed |
| Prompt html | `../logs/parser/<build>/html/<address>-trim.html`, exactly as the LM read it, for each such page sent to the LM |

- `checkLocation` builds a `ParseTracker` for each location and reports the event stage to it. When the check finishes, a report is written only if `needsReport()`, as stated under Links. A later check in the same build overwrites the report.
- A reader takes its tracker, never null, reports raw objects to it (the fetch and document, schemas tried or created, and its outcome at every exit of `read()`), and consults it before fetching. A reader computes no reported value.
- The tracker derives every reported value. A new stat is added in the tracker alone.
- The feed reader hands each event page reader the child `PageTracker` from `tracker.page(url)`.
- `PageTracker.collect` keeps any other object the tracker draws on when the report is built, such as the feed's event elements.
- A `PageTracker` is the `HtmlParseObserver` passed to `readHtml`, which fills the LM stage.
- A report holds the feed page, each event page read, and the events the feed yielded. Each page has one section per stage, as far as it got: `fetch`, `lm`, `schema`, and its `outcome` and HTTP `status`.

| Stage | Logged |
|---|---|
| Fetch | HTTP status, mode, final url, chars, visible text chars, time taken |
| LM | Model, trim stats, chars cut by the cap, attempts, tokens, time taken, raw response |
| Schema | Stored or new, stored schemas tried, validation result, fields dropped; for a feed, event count and matches per field |
| Events | Found, created, past, duplicates, failed creates, date text that did not parse |

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
- An event page is read again only when its link is `Granted` with no `parseOutcome` and content that is null or `Unread`: a read interrupted before classification, or content awaiting scripting. `wantsRead` states it.
- A feed is not read again when its link is not `Granted`, its content is `OffSchema`, `OffScope` or `Unknown`, or its `parseOutcome` is `Fail`. `stopsFeed` states it. A failure waits for a better build.
- An event whose date text does not parse marks the feed, and the page it was read from, `Partial`.
- A report is written, and html saved, for each check where some page is `Partial` or `Fail`, or has content other than `Schema`.

## Parse Limit

A check reads at most 30 event pages. A page past the limit is recorded as `deferred`, gets no link, and its event is dropped for this check, so the next check reads the page and builds the event in full. Pages skipped by their link do not count toward the limit, so a large feed is read in full over a series of checks.

## Fetch Mode

An origin is fetched in `Basic` mode until the LM reports a page of it as incomplete content, whether or not the content was the expected kind. `registerIncomplete` then moves the origin to `Scripting`, and its pages are fetched through Playwright from the next read on.

A `Scripting` fetch waits after the load event until the body's text has held steady for a second, up to 8 seconds, so content rendered late is included. It then folds each iframe holding text into the page as a `div` marked `data-frame-src`, with its relative links made absolute, since the page's html leaves out iframe contents.

## Events

A feed's events are created only with a start date and time that is still ahead. An event whose start cannot be parsed from its date and time text is dropped and its text reported under `unparsedDates`, and an event whose start has passed is dropped and counted under `past`. Feeds supply what they readily can; other events are posted by hand.

Date text is parsed by `parseLocalDateTime`, after every Unicode space is folded to a plain one. It reads a year-less date as the nearest such date to today, using a named weekday to choose among candidates.

## Strikes

`ParseTracker` counts consecutive strikes per origin within one check. A 4xx or 5xx response to a page adds a strike to the origin of the url requested, and a successful fetch from that origin clears them. At 3 strikes, `shouldFetch` is false, and the feed reader records the origin's remaining event pages as `benched`, whose events keep the data read from the feed.

## Schema Validation

A schema from the LM is validated against the page it was made from before it is stored. Each selector is run against the raw page.

| Schema | Must match | Otherwise |
|---|---|---|
| Feed | `event`, at least one element | Any other selector that is invalid or matches in no event is set to null |
| Page | `title` a plausible field, `description` plausible prose | Any other selector that is invalid or matches nothing is set to null |

A failed schema is not stored, and the page is recorded as `read-invalid-selector`. The page test is the one a stored schema must pass to be reused.

## Field Queries

A selector is expected to match a single element. `queryElement` takes the matches in document order and returns the first that has text or an image and passes the field's test: plausible prose for `description`, a plausible field for the other text fields. It returns null for an invalid selector.

- A field takes one element, never a join of several. A short description is preferred to one that gathers unrelated text.
- Matches with no text or image are skipped, since the LM reads html with empty elements trimmed.
- Parsing, schema validation and stored-schema reuse all read fields through `queryElement`, so they agree on what a selector yields.


## Workflows

Starting a new build:

1. Raise `parserBuildId`.
2. Dump the `parser` table to `logs/schema/parser-<timestamp>.json`.
3. Delete all rows of `parser`, `link` and `event`, clear `location.checked_at`, and reset each `origin` to `fetch_mode` Basic with no `robots_txt`, so the build reads every location from a fresh state.
