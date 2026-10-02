# Package streetlight.server.daemon.integration

## Introduction

Integration tests for the daemon, in the test source set. Each runs real collaborators together, along with the resources they need.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.server.daemon.agent` | The classifier and its embeddings client |
| `streetlight.server.daemon` | `getCachedVectors` |
| `kotlin.test` | Assertions |

`docs/testing.md` states how tests are written.

## Resources

A test raises the resources it needs once per run, in a `lazy` of its companion. A resource that cannot be raised throws an error naming what must be running, rather than skipping the test or letting it fail on an assertion.

| Test | Needs |
|---|---|
| `EntityClassifierTest` | Ollama at its default address, with the default embeddings model pulled |

## Classification

`EntityClassifierTest` classifies each event of the test resource `classified-events.json` and expects every one to be given its type. Each entry is a `ClassifiedEvent`: an observed event, as an `EventEdit` without the source note the crawler adds after classification, and the `EventType` a person gave it. `readClassifiedEvents` reads a resource and fails when it is missing.
