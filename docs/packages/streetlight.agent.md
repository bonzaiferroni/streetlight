# Package streetlight.agent

## Introduction

Work that talks to a language model: prompting, structured decoding, and the HTML trimming that keeps a page within a prompt.

## Dependencies

| Package | Provides |
|---|---|
| `ai.koog` | Prompting and structured decoding against the model |
| `com.fleeksoft.ksoup` | HTML parsing and trimming |
| `kampfire.model` | `Outcome` |
| `io.ktor.client` | Page fetching |

## HtmlParserClient

`HtmlParserClient` is the interface for reading structure out of a page. `KoogHtmlParserClient` implements it against Gemini; `TestHtmlParserClient`, in the server's test source set, stands in for it under test.

The interface declares the `KClass` and `KType` overloads. `readHtml<T>` is an inline extension on the interface, because a reified function cannot be a virtual member.

A caller holds the interface. Holding `KoogHtmlParserClient` reaches a language model in every environment.

## Caching

`KoogHtmlParserClient` caches a response against the hash of the document it parsed, and evicts the oldest entry past a fixed size. A repeated parse of the same page costs nothing.

## Rate Limiting

`KoogHtmlParserClient` spaces every model call, retries included, at least `callInterval` apart across all callers. A busy or rate-limited response (`503`, `UNAVAILABLE`, `429`) is retried after the `retryDelay` in its error body, or the client's `retryDelay` when it has none, up to the caller's `retryCount`, and then returns `LMProblem.Busy`.

## Trimming

`HtmlTrimmer` trims a copy of the document, so selectors from the model run against the untrimmed page, the same way a stored schema does on a later read.

A removal is added only for content that cannot carry event information. Elements, meta tags and attributes are removed by denylist, and everything not listed is kept.
