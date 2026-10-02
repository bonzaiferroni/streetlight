# Crawler Classifier Work

Notes for the work on classifying crawled events by `EventType` and `EventSubtype`, across sessions. Read the agent package document (`docs/packages/streetlight.server.daemon.agent.md`) and the integration test document (`…daemon.integration.md`) first.

## Where It Stands

* `EntityClassifier.classifyEvent` embeds an `EventEdit` once with `qwen3-embedding:0.6b` through Ollama, as a query with an instruction, and takes the `EventType` whose label vector has the highest cosine similarity, when it is at least 0.4. `deliverEvent` classifies after its guards and before the source note.
* Label vectors are cached in `data/embeddings`, keyed by model and label text.
* `EntityClassifierTest` classifies 10 observed events, one per type, from `daemon/src/test/resources/classified-events.json`. First run (2026-10-02): 9 of 10. Evening Yoga was given `Dance`, its description sharing "class" and "movement" with the Dance label while the Fitness label names neither yoga nor classes.
* Subtypes are not yet classified. Which subtypes each type can show is not yet defined.
* Classification problems are not yet tracked on the `ParseTracker`.

## Stages

Each stage reuses the vectors of the one before; nothing upstream of the embedding changes.

1. **Cosine against label descriptions.** No examples needed. Current.
2. **Cosine against centroids** of labeled example vectors, one centroid per type.
3. **Logistic regression** trained on the same example vectors, giving a probability per type.

A trained layer is tied to the embedding model. Labeled examples are kept as raw text, so a model change re-embeds them and retrains.

## Next

* Take another pass at every classifier label, starting with Fitness.
* Build a labeled example set large enough for stages 2 and 3.

## Open Questions

* The threshold of 0.4 is a guess. A confidence from the margin between the top two scores, or a probability from stage 3, may replace it.
* A shortened description ends with a "Read more" link, which reaches the embedding.
