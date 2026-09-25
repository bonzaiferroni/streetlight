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
| Sibling runs | Past the tenth consecutive sibling sharing a tag and class, the rest are removed |

A run is collapsed only in the prompt copy. Selectors run against the full page, so every item in the run is still read.

`KoogHtmlParserClient` cuts the trimmed html to `LmConfig.htmlCharLimit` before building the prompt, so the instructions always reach the model. A provider that truncates an oversized prompt itself may drop them.

`HtmlTrimmer.trimHtml` returns a `TrimResult` of the html and its `TrimStats`.

## Call Observer

`readHtml` takes an optional `HtmlParseObserver`, and the client reports to it as the call proceeds: the `TrimResult` with the prompt html exactly as the model reads it, and the response with its tokens, time and attempts. A caller that passes none is unaffected.
