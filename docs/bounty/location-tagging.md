# Location Tagging

The crawler gives each location it creates its `LocationTag`s, as it gives events their `EventTag`s. Backfills and UI rendering are later bounties.

## Shape

Event tagging, mirrored:

| Event | Location |
|---|---|
| `EventTable.tags`, `int[]` with a GIN index | `LocationTable.tags` |
| `EventEdit.tags`, `Event.tags` | `LocationEdit.tags`, `Location.tags` |
| `EventEdit.getEmbeddingsText(ldType)` | `LocationEdit.getEmbeddingsText()`, led by its map category and type |
| `eventInstruction` | `locationInstruction` |
| `EventTag.getClassifierLabel()` in `TagAnnotation.kt` | `LocationTag.getClassifierLabel()` |
| `event-examples.json`, `event-evaluation.json` | `location-examples.json`, `location-evaluation.json` |
| `EventClassification`, `RankedTag` | `LocationClassification` |
| `EntityClassifier.classifyEvent` | `EntityClassifier.classifyLocation` |
| `trainTagModels` over `EventTag` | Generic over the tag enum |
| `EntityClassifierEvaluationTest` | A location case beside it |
| `deliverEvent` classifies before `createEventAt` | `LocationSpawner` classifies before each create |

The classifier does not ask an LM which tag a location deserves. The embedding model (`qwen3-embedding:0.6b`) turns each location's text into a vector, and a logistic regression per tag, trained at start on hand-labeled examples, decides each tag. The labeled examples are the bulk of the work.

## Steps

1. 🍻 Model and table: `LocationTable.tags`, `Location.tags`, `LocationEdit.tags`, the reads and writes. Migration left to the navigator.
2. 🍻 Classifier: generic tag models, `LocationClassification`, `classifyLocation`, `locationInstruction`, `getEmbeddingsText`, `raiseEntityClassifier` trains both.
3. 🍻 Annotation guide: `LocationTag.getClassifierLabel()`.
4. 🍻 Example and evaluation sets, labeled from the dev database.
5. 🍻 Crawler: classify in `LocationSpawner` and in `mergeAndUpdateLocation`, tracked on the page.
6. 🍻 Evaluation test: locations score macro F0.5 0.479 over 85 tags, floor set at 0.475; events hold at 0.720.
7. 🍻 Docs.

## Decisions

- `LocationEdit.tags` defaults to empty and `Location.tags` is nullable, as on events. `Location.toEdit()` carries the tags, so a form edit keeps them, and `mergeLeft` keeps the left's unless it has none.
- A location's text is led by `Category: <mapCategory>, <mapType>`, its stand-in for an event's JSON-LD type. JSON-LD place types are not read yet.
- The classifier reads a location without its source note, since the note is added before classification. `classifyCrawledLocation` strips it and keeps the full edit.
- Classification happens in `LocationSpawner.create` and in the distinct-place path, inside the write, after the stored-location check, so a location found stored is not classified. A location with no name, created at an address, is skipped.
- `mergeAndUpdateLocation` classifies a merged edit only when the stored location has no tags.
- Location classifications are tracked on the lead's page, counted as `locationsClassified`.
- `trainTagModels` became `trainTagModelSet`, generic over the tag type, and `toCentroid` moved beside it into `TagModel.kt`. `readTags` became `readEventTags`.
- Labels: all 814 named dev locations, by `LocationTagAnnotation.kt`. Broad tags: `Bar` (not for producers), `Restaurant`, `PlaceOfWorship`, with the "Also" rules applied mechanically. Locations no tag fits, such as a car dealer or a city, keep no tags as negatives.
- Split: 657 examples, 157 held out, seeded, identical texts kept on one side, and every tag with three or more labels given at least two examples. 102 tags get models; the rest are never given until examples arrive.
- `Shop` became `Shopping` (2026-10-11), a broad tag: every store kind, a mall or a market is also Shopping. Tags are additive, so a broad tag sits beside the narrow one. Shopping scored F0.5 0.87 over 11 held-out locations, and the macro rose from 0.479 to 0.487.
- Federal Theatre's stored description is placeholder text ("# Yer title Yer content."), left out of its example.

- `EventTag.Halloween` and `EventTag.Party` (formerly `WatchParty`) have annotations; the event sets' `WatchParty` labels were renamed to `Party`. No event carries `Halloween` yet, so it has no model.

## Findings

- Common location tags score well: `Library`, `PlaceOfWorship`, `Church`, `Hotel`, `School`, `Mall` at 1.00; `Park` 0.98, `Restaurant` 0.88, `Bar` 0.81, `Cafe` 0.81.
- The macro average is held down by tags with one or two held-out locations and two or three examples, which score 0. More examples of rare kinds raise it most.
- `MusicVenue` is the weakest common tag (P 0.38, R 0.86): the line between a bar with music and a music venue is fuzzy in the text.
