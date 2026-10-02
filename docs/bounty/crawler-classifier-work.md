# Crawler Classifier Work

Notes for the work on classifying crawled events by `EventType` and `EventSubtype`, across sessions. Read the agent package document (`docs/packages/streetlight.server.daemon.agent.md`) and the integration test document (`…daemon.integration.md`) first.

## Where It Stands

* `EntityClassifier.classifyEvent` embeds an `EventEdit` once with `qwen3-embedding:0.6b` through Ollama, as a query with an instruction, and takes the `EventType` whose label vector has the highest cosine similarity, when it is at least 0.4. `deliverEvent` classifies after its guards and before the source note.
* Label vectors are cached in `data/embeddings`, keyed by model and label text.
* `EntityClassifierTest` classifies 10 observed events, one per type, from `daemon/src/test/resources/classified-events.json`. First run (2026-10-02): 9 of 10. Evening Yoga was given `Dance`, its description sharing "class" and "movement" with the Dance label while the Fitness label names neither yoga nor classes.
* `daemon/src/main/resources/event-examples.json` holds 273 observed events labeled by type and subtype (2026-10-02, first pass): Meetup 43, Fitness 29, Youth 29, Education 25, FoodAndDrink 25, Music 24, Sports 21, Dance 18, Arts 18, Comedy 16, Church 16, Volunteer 9. Volunteer is thin in the data; more examples want a broader search or new leads.
* Subtypes are classified on their own, from the same vector as the type, with a minimum similarity of 0.5. Which subtypes each type can show is not yet defined, and the type does not narrow the subtype.
* Classification problems are not yet tracked on the `ParseTracker`.

## Priority

One type and one subtype per event, the most relevant value. The description carries the rest.

* `Youth` comes first: it names who the event is for. `Church` comes next. The other types have roughly equal priority.
* Subtypes have roughly equal priority. When an event fits two, such as a picnic that is a potluck, the one its title or description names foremost wins.
* Priority lives in how examples are labeled. If it wants code, its home is `EntityClassifierLabels.kt`, as a tie-break toward the higher type when scores are close.

## Value Survey

On 2026-10-02 the 2,129 distinct event titles were sampled and counted by keyword. `Arts` was added as a type, and `DJSet`, `GameNight`, `Lecture`, `OpenPlay` and `Festival` as subtypes, each seen in roughly 50 to 100 events. Subtypes are grouped in `EventSubtype` by the type they generally fall under.

Runners-up, for a later pass:

* Types: Outdoors (hikes, bird walks, paddling, group rides; about 110), Food & Drink (food trucks, tastings; about 50).
* Subtypes: Meditation (meditation, sound baths, breathwork; about 34), Tasting (about 29), Food Truck (about 20), Group Ride and Group Run (about 30 to 40 each).

Music is undercounted by keyword, since most concert titles are only the artist's name.

## Stages

Each stage reuses the vectors of the one before; nothing upstream of the embedding changes.

1. **Cosine against label descriptions.** No examples needed. Kept as the fallback for a value with no examples.
2. **Cosine against centroids** of labeled example vectors, one centroid per type. Current.
3. **Logistic regression** trained on the same example vectors, giving a probability per type.

A trained layer is tied to the embedding model. Labeled examples are kept as raw text, so a model change re-embeds them and retrains.

## Next

* Take another pass at every classifier label, starting with Fitness.
* Build a labeled example set large enough for stages 2 and 3, about 20 per type, labeled by the priority above.
* Define which subtypes each type can show.

## Open Questions

* A subtype is optional, and 102 of the 273 examples have none. Their vectors could form a "no subtype" centroid, so an event nearest it gets none, in place of the 0.5 threshold.
* `EventTable` stores `eventType` and `eventSubtype` by ordinal. Once values are stored, a value added inside a group of `EventSubtype` renumbers those after it. Before the first classified event is stored, the columns want `enumerationByName`, or the enums become append-only.
* The threshold of 0.4 is a guess. A confidence from the margin between the top two scores, or a probability from stage 3, may replace it.
* A shortened description ends with a "Read more" link, which reaches the embedding.
