# Module daemon

## Introduction

A standalone process that runs the crawler beside the server, sharing its database.

## Packages

| Package | Holds |
|---|---|
| [`streetlight.server.daemon`](src/main/kotlin/streetlight/server/daemon/README.md) | The process's entry |
| [`streetlight.server.daemon.crawler`](src/main/kotlin/streetlight/server/daemon/crawler/README.md) | The crawler, which reads event feeds and pages and creates the events it finds |
| [`streetlight.server.daemon.agent`](src/main/kotlin/streetlight/server/daemon/agent/README.md) | The crawler's work with a language model |
| [`streetlight.server.daemon.integration`](src/test/kotlin/streetlight/server/daemon/integration/README.md) | Tests that run the daemon's collaborators together, with the resources they need |
| [`streetlight.server.daemon.unit`](src/test/kotlin/streetlight/server/daemon/unit/README.md) | Tests of the rules of one function or type, with no collaborators |
