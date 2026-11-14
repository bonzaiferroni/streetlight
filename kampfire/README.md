# Module kampfire

## Introduction

Utilities and types for apps that communicate with a server in a multiplatform context. Everything is in `commonMain`, so the client and the server hold the same code.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.serialization` | Serialization of wire types |
| `kotlinx.coroutines` | `Flow`, behind `Store` and `Tap` |

## Packages

| Package | Holds |
|---|---|
| `kampfire.api` | Value types that cross the wire, such as `Markdown`, `Username`, `Slug`, endpoint definitions, and the transport calls go through |
| `kampfire.model` | Requests, results, and the state and messaging types built on them, such as `Outcome`, `Messenger`, `Store`, and `Tap` |
| `kampfire.utils` | General extensions with no domain in them |

## Fuzzy Matching

`String.fuzzyMatches` decides whether two texts name the same thing, such as two listings of one event. It compares words, each word of the text with fewer words to its closest word in the other by Levenshtein distance, so added words and small spelling differences both still match. It has no rules for any one language, and is the matcher to reuse wherever two names are compared.

## Shared Types

A type that both sides send, receive, or store belongs here.

## Outcome

A call that can fail returns `Outcome`. It is either `Ok` with data and an optional message, or `Problem` with a message. The caller unwraps it with `toDataOr`, which delivers the message to a `Messenger` and lets the caller decide how to stop.

## Interface Here, Implementation Elsewhere

A type that describes a behavior the UI performs is declared here as an interface, and the implementation that binds to the DOM lives in `koala`. `Messenger` is declared here, and `MessageStore` implements it in `koala.model`.

## Transport

`ApiTransport` carries calls to an API and opens its streams over a platform's HTTP. An app implements it once per platform, and its clients are written once against it.

| Part | Role |
|---|---|
| `ApiCall` | One call: the endpoint, the id or body sent, the query parameters, a form when it uploads, and the serializers of both ends |
| `getApi`, `postApi`, `postForm` | Build an `ApiCall` from an endpoint and send it |
| `openEvents`, `openSocket` | Open an event stream as a `Flow`, or a socket as a `MessageSocket`, carrying JSON |
| `TestTransport` | The transport of tests, answering each endpoint with the block given to `answer` |

A transport decodes a response as the CBOR `Outcome` the server writes, and sends a body as JSON. A socket holds sends until it opens.
