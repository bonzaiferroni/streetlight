# Package streetlight.server.daemon.integration

## Introduction

Integration tests for the daemon, in the test source set. Each runs real collaborators together, along with the resources they need.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.server.daemon.agent` | The classifier and its embeddings client |
| `streetlight.server.daemon` | `raiseEntityClassifier` and `getCachedVectors` |
| `kotlin.test` | Assertions |

`docs/testing.md` states how tests are written.

## Resources

A test raises the resources it needs once per run, in a `lazy` of its companion. A resource that cannot be raised throws an error naming what must be running, rather than skipping the test or letting it fail on an assertion.

## Classification

`event-evaluation.json`, a test resource, is the evaluation set: observed events annotated from their full descriptions, held out from the example set, so a change to the classifier is scored against events it was not built from. It is in the format of the example set described in `streetlight.server.daemon.agent`.

`EntityClassifierEvaluationTest` classifies each event of the evaluation set with its tags cleared, through `readTags` on vectors cached as `EventEvaluation`, and prints the precision, recall and F0.5 of each tag the set holds. It asserts the average F0.5 across those tags is at least the floor the current example set and pipeline reach, so a change that lowers it fails; the floor is raised as they improve. It needs Ollama at its default address with the default embeddings model.
