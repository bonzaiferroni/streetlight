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
| Page html | `../logs/parser/<build>/html/<address>.html` |
| Prompt html | `../logs/parser/<build>/html/<address>-trim.html`, exactly as the LM read it, for each page sent to the LM |

- `checkLocation` builds a `ParseTracker` for each location and reports the event stage to it. When the check finishes, `tracker.report()` builds the `ParseReport` and `write` saves it. A later check in the same build overwrites it.
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

| Outcome | Meaning |
|---|---|
| `skipped` | The page was not fetched, since its link was already read |
| `benched` | The page was not fetched, since its origin is benched |
| `blocked` | robots.txt disallows the path, and no html is cached |
| `unreachable` | The fetch failed, and no html is cached |
| `no-html` | The html did not parse |
| `limit` | The LM usage limit is reached |
| `lm-error` | The LM failed for any other reason |
| `read-no-content` | The page is not the expected content |
| `read-invalid-selector` | A new LM schema failed validation, or the feed event selector matched nothing |
| `read-content` | The expected content was read |

## Fetch Mode

An origin is fetched in `Basic` mode until the LM reports a page of it as incomplete content, whether or not the content was the expected kind. `registerIncomplete` then moves the origin to `Scripting`, and its pages are fetched through Playwright from the next read on.

A `Scripting` fetch waits after the load event until the body's text has held steady for a second, up to 8 seconds, so content rendered late is included. It then folds each iframe holding text into the page as a `div` marked `data-frame-src`, with its relative links made absolute, since the page's html leaves out iframe contents.

## Events

A feed's events are created only with a start date and time that is still ahead. An event whose start cannot be parsed from its date and time text is dropped and its text reported under `unparsedDates`, and an event whose start has passed is dropped and counted under `past`. Feeds supply what they readily can; other events are posted by hand.

Date text is parsed by `parseLocalDateTime`, which reads a year-less date as the nearest such date to today, using a named weekday to choose among candidates.

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
