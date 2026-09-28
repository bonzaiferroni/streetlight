# Package streetlight.server.daemon.agent

## Introduction

The crawler's work with a language model: prompting, structured decoding, the schema mediator, and the HTML trimming that keeps a page within a prompt.

## Dependencies

| Package | Provides |
|---|---|
| `ai.koog` | Prompting and structured decoding against the model |
| `com.fleeksoft.ksoup` | HTML parsing and trimming |
| `kampfire.model` | `Outcome` |
| `io.ktor.client` | Page fetching |

## General Model

The parsing in this package follows the General Model of `streetlight.server.daemon.crawler`.

## HtmlParserClient

`HtmlParserClient` is the interface for reading structure out of a page. `KoogHtmlParserClient` implements it against the model named by its `LmConfig`; `TestHtmlParserClient`, in the server's test source set, stands in for it under test.

The interface declares the `KClass` and `KType` overloads. `readHtml<T>` is an inline extension on the interface, because a reified function cannot be a virtual member.

A caller holds the interface. Holding `KoogHtmlParserClient` reaches a language model in every environment.

## Model Configuration

`LmConfig` holds the model, and the key, address and request properties needed to reach it. `Environment.readLmConfig()` builds it from these keys.

| Key | Default | Used by |
|---|---|---|
| `LM_PROVIDER` | `google` | Both; `google` or `ollama` |
| `GEMINI_KEY_A` | none | `google` |
| `LM_MODEL` | `qwen3.5:9b` | `ollama` |
| `LM_CONTEXT_LENGTH` | `32768` | `ollama`, sent as the context window of every request |
| `LM_BASE_URL` | `http://localhost:11434` | `ollama` |

An Ollama model runs with thinking turned off.

The parser runs on `qwen3.5-parser`, a model derived from `qwen3.5:9b` that adds `PARAMETER num_predict 1024` to cap the response length. Koog does not forward a maximum token count to Ollama, so the cap lives in the model. Its Modelfile is `~/apps/ollama/qwen3.5-parser.Modelfile` and is built with `ollama create qwen3.5-parser -f <Modelfile>`.

## Structured Output

`KType.toStandardSchema()` builds the response schema, sent as a standard JSON schema to every provider. Every property is required, and a nullable property is typed as its type or `null`, so a model cannot skip a field by leaving it out. The parser samples at a temperature of 0.1.

## Rate Limiting

`KoogHtmlParserClient` spaces every model call, retries included, at least the `LmConfig.callInterval` apart across all callers. A busy or rate-limited response (`503`, `UNAVAILABLE`, `429`) is retried after the `retryDelay` in its error body, or the client's `retryDelay` when it has none, up to the caller's `retryCount`, and then returns `LMProblem.Busy`.

## Trimming

`HtmlTrimmer` trims a copy of the document, so selectors from the model run against the untrimmed page, the same way a stored schema does on a later read.

A removal is added only for content that cannot carry event information. Elements, meta tags and attributes are removed by denylist, and everything not listed is kept.

Hidden content is kept, since pages hide events in modals, tabs and collapsed sections.

| Measure | Rule |
|---|---|
| Attribute values | Whitespace collapsed in every value |
| `data-*` attributes | Removed when the value is over 200 characters |
| Sibling runs | Past the tenth consecutive sibling sharing a tag and first class, the rest are removed |

A run is collapsed only in the prompt copy. Selectors run against the full page, so every item in the run is still read.

`KoogHtmlParserClient` cuts the trimmed html to `LmConfig.htmlCharLimit` before building the prompt, so the instructions always reach the model. A provider that truncates an oversized prompt itself may drop them.

`HtmlTrimmer.trimHtml` returns a `TrimResult` of the html and its `TrimStats`.

## Call Observer

`readHtml` takes an optional `HtmlParseObserver`, and the client reports to it as the call proceeds: the `TrimResult` with the prompt html exactly as the model reads it, and the response with its tokens, time and attempts. A caller that passes none is unaffected.

## LM Instructions

`SchemaParserText` holds the instructions sent to the language model, and lives beside the client that sends them. The instructions are written for a capable model and state what is wanted, not how one model tends to go wrong. A mistake a particular model makes, such as malformed CSS, is caught by validation rather than answered with an instruction, so the instructions carry over to another model.

A feed's instructions differ by its kind of `EventFeed`: a location's feed asks for `feedLocation` and for an `eventLocation` when an event names a room or another venue, and a general feed asks for an `eventLocation` in every event. `feedSelectorsInstructions` picks them.

A request to the LM asks for one shape at a time. A follow-up request asks a narrow question with its own instructions, such as the parts of an event's start, rather than widening the first request.

## Schema Mediator

`SchemaMediator` finds the schema of a feed, an event page or a location's homepage, and is the only caller that asks the LM for one. The crawler is its only user; the server's parse endpoints are retired.

1. The schemas stored for the page's origin, in the `parser` table, are tried first. The first feed schema whose `event` selector matches is used, and of the page schemas whose title passes, the one with the longest description.
2. Otherwise the LM is asked for the whole schema with `EventFeedSchemaRequest` or `EventPageSchemaRequest`, and the answer is validated.
3. When the start of the events does not parse, the LM is asked the time follow-up with `EventTimeSchemaRequest`: the month, the day and the start time, each alone.
4. When a new page schema has no description, the LM is asked the description follow-up with `EventDescriptionSchemaRequest`, and its answer is kept only when the page reads as plausible prose through it.
5. The result is stored for the origin, under its fetch mode, and returned. A page the LM finds incomplete marks its origin for scripting.

The request types are what the LM sees, and `EventFeedSchema` and `EventPageSchema` are what is stored. A stored schema holds what every request found, so a field such as `month` is never part of the first request.

A `SchemaObserver` is told of each request to the LM, each stored schema tried, and each schema created.

A selector the LM writes as text for a null, such as `"null"` or `"none"`, is read as null before validation.

| Time part | Kept when |
|---|---|
| `month` | Every text it matches names a month |
| `day` | Every text it matches holds a single number from 1 to 31 and no other number, and the texts differ across a feed's events |
| `startTime` | Every text it matches parses as a time |

A part must match in at least half of the events sampled. The refined schema is kept only when the starts of at least half the events then parse, run in list order, and fall within 400 days of now. Otherwise the schema from the first request is kept.

## Schema Validation

A schema from the LM is validated against the page it was made from before it is stored. Each selector is run against the raw page.

| Schema | Must match | Otherwise |
|---|---|---|
| Feed | `event`, at least one element | Any other selector that is invalid or matches in no event is set to null |
| Page | `title` a plausible field | Any other selector that is invalid or matches nothing is set to null, `description` included when it matches no plausible prose |

A failed schema is not stored, and the crawler records the page as `Schema` content with a `Fail` outcome. The page test is the one a stored schema must pass to be reused. A page without a description is still read for its event data, and its url serves as the event's website, where a person can read what the parser missed.

## Location Schema

A `LocationSchema` reads a location's homepage toward a `LocationEdit`: its name, description, whole address as one block, phone, email, hours, the link to its events page, an image, and the list of its social links. The phone and email are read only from the location's own homepage, where publishing them states they are public.

`SchemaMediator.locationSchema` finds it the way it finds a page's schema, without follow-ups. A stored schema is reused while its `name` still matches. A new one needs a `name` that matches, or it is invalid; each other field is kept when it matches, `description` as plausible prose, `eventsLink` and each of `socialLinks` with an href (on any site), and `image` with a src. A location's fields are not refused for sitting in a header or footer, where a homepage keeps them.

## Field Queries

A selector is queried against the whole document, head included, and is expected to match a single element; a list selector, such as a feed's `event` or a location's `socialLinks`, matches many. `queryElement` takes the matches in document order and returns the first that has text, an image or a meta element's content, and passes the field's test: plausible prose for `description`, a plausible field for the other text fields. It returns null for an invalid selector.

- A field takes one element, never a join of several. A short description is preferred to one that gathers unrelated text.
- Matches with no text, image or meta content are skipped, since the LM reads html with empty elements trimmed.
- A meta element is read by its `content` attribute in place of its text, so a head value such as `og:site_name` can fill a field.
- Parsing, schema validation and stored-schema reuse all read fields through `queryElement`, so they agree on what a selector yields.
- An event page's image is the image its meta declares for outside links (`og:image`, `twitter:image`, `image`) when it has one, and the schema's `image` otherwise. A feed's image comes from its schema alone.
