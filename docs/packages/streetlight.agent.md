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

## Caching

`KoogHtmlParserClient` caches a response against the hash of the document it parsed, and evicts the oldest entry past a fixed size. A repeated parse of the same page costs nothing.

## Rate Limiting

`KoogHtmlParserClient` spaces every model call, retries included, at least the `LmConfig.callInterval` apart across all callers. A busy or rate-limited response (`503`, `UNAVAILABLE`, `429`) is retried after the `retryDelay` in its error body, or the client's `retryDelay` when it has none, up to the caller's `retryCount`, and then returns `LMProblem.Busy`.

## Trimming

`HtmlTrimmer` trims a copy of the document, so selectors from the model run against the untrimmed page, the same way a stored schema does on a later read.

A removal is added only for content that cannot carry event information. Elements, meta tags and attributes are removed by denylist, and everything not listed is kept.

Hidden content is kept, since pages hide events in modals, tabs and collapsed sections.

`KoogHtmlParserClient` cuts the trimmed html to `LmConfig.htmlCharLimit` before building the prompt, so the instructions always reach the model. A provider that truncates an oversized prompt itself may drop them.
