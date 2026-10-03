# Package streetlight.server.daemon

## Introduction

The daemon module: a standalone process that runs the crawler beside the server, sharing its database.

## Structure

| Package | Holds |
|---|---|
| `streetlight.server.daemon` | `Main`, which builds the server's services and the LM client and starts the crawler |
| `streetlight.server.daemon.crawler` | The crawler: its feeds, service functions, location spawning, reports and fetching |
| `streetlight.server.daemon.agent` | The crawler's work with a language model: the client, instructions, schema mediator, validation and trimming |
| `streetlight.server.daemon.tools` | One-off entry points, each run by its own Gradle task, such as `backfillTags`, which tags every stored event without tags |
| `streetlight.server.daemon.unit` | Unit tests, in the test source set |
| `streetlight.server.daemon.integration` | Integration tests, in the test source set, which need the resources they raise |

The LM client is built in `Main` and handed to the `Crawler`, not provided by the server's container, since the crawler is its only user.
