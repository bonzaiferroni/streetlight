T# kampfire

Utilities and types for apps that communicate with a server in a multiplatform context. Everything is in `commonMain`, so the client and the server hold the same code.

`kampfire` depends on no UI or DOM module. `koala` depends on it.

## Packages

| Package | Holds |
|---|---|
| `kampfire.api` | Value types that cross the wire, such as `Markdown`, `Username`, `Slug`, and endpoint definitions |
| `kampfire.model` | Requests, results, and the state and messaging types built on them, such as `Outcome`, `Messenger`, `Store`, and `Tap` |
| `kampfire.utils` | General extensions with no domain in them |

## Shared Types

A type that both sides send, receive, or store belongs here.

## Outcome

A call that can fail returns `Outcome`. It is either `Ok` with data and an optional message, or `Problem` with a message. The caller unwraps it with `toDataOr`, which delivers the message to a `Messenger` and lets the caller decide how to stop.

## Interface Here, Implementation Elsewhere

A type that describes a behavior the UI performs is declared here as an interface, and the implementation that binds to the DOM lives in `koala`. `Messenger` is declared here, and `MessageStore` implements it in `koala.dom`.
