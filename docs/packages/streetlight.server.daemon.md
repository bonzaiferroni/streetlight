# Package streetlight.server.daemon

## Introduction

A standalone process that reads location event feeds and the event pages they link to, and creates the events it finds.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.server.model` | `Server`, the DAO and event creation |
| `streetlight.agent` | Fetching, html parsing and the LM schema parser |

## Check Logs

Each pass of `ParseDaemon.start()` is a check, and writes one report whether or not it read anything. Paths are relative to the `daemon` working directory.

| Output | Path |
|---|---|
| Check report | `../logs/parser/<timestamp>.log` |
| Page html | `../logs/parser/html/<outcome>/<address>.html` |
| Trimmed page html | `../logs/parser/html/<outcome>/<address>-trim.html`, for every page whose html parsed |

- The report opens with a summary of the check, followed by a summary of each feed.
- Every page that returns html is cached under its outcome, and a later read of the same address replaces it.
- A reader records its outcome at every exit of `read()`, through its `FeedReport` or `PageOutcome`.

| Outcome | Meaning |
|---|---|
| `skipped` | The page was not fetched |
| `blocked` | robots.txt disallows the path, and no html is cached |
| `unreachable` | The fetch failed, and no html is cached |
| `no-html` | The html did not parse |
| `limit` | The LM usage limit is reached |
| `lm-error` | The LM failed for any other reason |
| `read-no-content` | The page is not the expected content |
| `read-invalid-selector` | The feed event selector matched nothing |
| `read-content` | The expected content was read |
