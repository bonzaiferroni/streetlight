# Crawler Classifier Work

Notes for the work on classifying crawled events by `EventType` and `EventSubtype`, across sessions. Read the agent package document (`docs/packages/streetlight.server.daemon.agent.md`), the integration test document (`…daemon.integration.md`) and `docs/bounty/crawler-work.md` (the build cycle) first.

## Direction: Tags

Decided 2026-10-03: the type/subtype pair gives way to tags. An event has zero, one or several values of one `EventTag` enum, in one `eventTags` column. Some tags are broad (the current types), some narrow (the current subtypes); the UI presents broad tags first and narrows within them. The column is an int array of ordinals with a GIN index, queried with `@>` (has a tag) and `&&` (has any of these). Tags are append-only by ordinal; a rename is free.

Broad and narrow is a distinction of the UI: choosing a broad tag puts its narrow tags first among the next filter's options, without hiding the others, since an event can join any tags (Sports and Picnic, Sports and Film). Classification uses the hierarchy only where it is shown to help.

The evaluation set and the example set are each a `List<EventEdit>`, the gold tags in each edit's `tags`: `[{ "title": …, "description": …, "tags": ["Film", "Festival"] }]`. A test clears an event's tags before classifying it and compares the result against them. A tag present is a yes and a tag absent is a no, so every entry is annotated against every tag; adding or renaming a tag takes a pass over the file. The set aims at 20 to 50 events carrying each tag; the observed data will fall short for some tags, and those are noted here as they are found, for the navigator to find leads for.

The reason: an event is rarely described by two labels, and a forced single choice puts a film festival under Festival or Film but not both, so a search for either misses it.

## Where It Stands

* **Shape changed (2026-10-03):** `Event`, `EventEdit` and `EventLocation` carry `tags: List<EventTag>`, stored in the `tags` int-array column with a GIN index. `ClassifiedEvent`, `classified-events.json` and the happy-path test are retired.
* **Regression per tag (2026-10-03):** each tag with at least two examples gets a logistic regression on the centered, unit-length vectors, trained at startup by gradient descent (400 iterations, rate 2.0, C = 1, balanced class weights). A tag is given at probability 0.7 or more. `EntityClassifierEvaluationTest` scores it on the evaluation set: the Kotlin port measured 0.720, every tag matching the prototype, and the floor is 0.715, so any degradation fails.

### Rule Comparison

Prototyped in a scratch Python script (numpy, scikit-learn) on the same embeddings, hyperparameters chosen by 5-fold cross-validation on the example set, then scored once on the evaluation set. Macro F0.5 averages the tags present in the evaluation set; micro pools every decision.

| Rule | Chosen by CV | Eval macro F0.5 | Eval micro P / R |
|---|---|---|---|
| Centroid, cosine threshold | threshold 0.81 | 0.544 | 0.50 / 0.56 |
| Centered centroid, threshold | threshold 0.475 | 0.639 | 0.74 / 0.52 |
| kNN, centered | k = 3, 40% of votes | 0.666 | 0.80 / 0.65 |
| Logistic regression per tag, centered | C = 1, probability 0.7 | 0.720 | 0.82 / 0.66 |

Plain gradient descent with the settings above reproduced the scikit-learn result (0.720), so the Kotlin port follows it.

Weakest tags under regression: Picnic, Potluck, Karaoke and Community Outreach score 0 (two to five examples each, or none of their evaluation events found); then Fundraiser 0.41, LGBTQ 0.51, Theater 0.54, Festival 0.55, Volunteer 0.56, Hike 0.59, Church 0.61 (recall 0.35). Strongest: Sports 0.98, Books & Writing and Games & Trivia 0.96, Food Truck, Pickup Game and Sports Match 0.94, Film 0.93. Recall is the general weakness (0.66 pooled), which fits the 0.7 threshold and the rule preferring missed information.


The notes below the shape change describe the type/subtype prototype.

* `EntityClassifier.classifyEvent` embeds an `EventEdit` once with `qwen3-embedding:0.6b` through Ollama, as a query with `eventInstruction`, and ranks every type and subtype by cosine similarity to its vector. The best of each is given when it reaches its minimum (type 0.4, subtype 0.5). `deliverEvent` classifies after its guards and before the source or RSVP note. `crawlEventLead` does not classify.
* `raiseEntityClassifier` (`MainUtility.kt`) builds each value's vector as the centroid of its examples, falling back to the vector of its classifier label for a value with no examples (today Picnic, Potluck and StreetPerformance). With every type having examples, the type labels no longer steer classification; only the examples do.
* Label and example vectors are cached in `data/embeddings` (`EventType-*`, `EventSubtype-*`, `EventExamples-*`), keyed by model, instruction and text.
* `daemon/src/main/resources/event-examples.json` holds 273 observed events labeled by type and subtype (2026-10-02, first pass, labeled by title and a glance at the description): Meetup 43, Fitness 29, Youth 29, Education 25, FoodAndDrink 25, Music 24, Sports 21, Arts 18, Dance 18, Church 16, Comedy 16, Volunteer 9.
* `EntityClassifierTest` classifies the 10 held-out events of `daemon/src/test/resources/classified-events.json` and requires all 10. Green with centroids.
* A page's schema.org event type, when more specific than `Event`, reaches the embedding as a `Category` line (`ParseProperty.DeclaredType`). In the saved pages of builds V30 to V36, about 320 JSON-LD blocks declared one against 5,559 plain `Event`. The examples were embedded without it.
* Every classification is reported on its page in the parse report (`classifications[]`), with the best type, runner-up and best subtype and their similarities. A check that classified anything writes its report. `records` counts `classified` and `unclassified`.

## V37 Results

`logs/schema/event-classification-V37.json` preserves the prototype's result: every V37 event (2,334) with its id, slug, title, url, location slug and start, and its `event_type` and `event_subtype` by name, mapped from the ordinals of the two enums as they stood in V37. The scores of each classification are in the V37 parse reports.

Build V37 was a full rescan (2026-10-02). From 1,947 classifications in its reports, read mid-run:

* The thresholds filter nothing: type similarity runs 0.71 (p10) to 0.89 (p90), median 0.83, so all 1,947 types and 1,943 subtypes were given.
* The margin between the top type and the runner-up is the signal: median 0.06, a tenth under 0.01. The misses cluster at low margins.
* Music and Arts are the main leak. Arts took 403 events, many of them bands with bare names (Teddy Swims, Leonid & Friends, she's green) as Theater. Brian Regan (comedy) went to Music.
* Church claims meditation by a hair: Mindfulness Meditation was Church over Fitness by 0.01. Meditation was the subtype of 134 events.
* Repeats inflate counts: the 13 time slots of "Trick or Treat Nature Trail" were all Volunteer / Fundraiser at a 0.03 margin.
* Only 38 events carried a declared type (26 MusicEvent, 12 EducationEvent).
* These feeds supplied the example set, so some events are the examples themselves, and their scores flatter the classifier.
* "Disney Descendants Zombies & Camp Rock: Worlds Collide" was Music / Concert, which is right: it is a concert tour. Judge by the description, not the title.

## Example Space

Measured on the 273 examples (2026-10-02):

* Closest centroids: Arts–Music 0.88, Arts–Comedy 0.88, Comedy–Music 0.87, Education–Youth 0.87, FoodAndDrink–Meetup 0.86.
* Spread, the mean similarity of a type's examples to its own centroid: Education 0.78 (widest) to Dance 0.90 (tightest). Neighboring centroids sit about as close to each other as examples sit to their own, so the clouds overlap.
* Leave-one-out accuracy, each example classified against centroids built without it: 231 of 273 (85%). Top confusions, true to given: Youth→Education 5, Fitness→Church 3, then pairs of 2 (Arts→FoodAndDrink, Dance→Music, Fitness→Dance, Meetup→FoodAndDrink, Meetup→Music, Meetup→Volunteer, Volunteer→Arts).
* Centering, subtracting the mean of all example vectors from each vector before comparing, drops the closest centroid pair from 0.88 to 0.37 and leaves accuracy unchanged (230). Every event embedding shares a common direction; centering removes it, so similarities and margins become readable. Worth adopting before tuning an abstain gate.

## Priority

One type and one subtype per event, the most relevant value. The description carries the rest.

* `Youth` comes first: it names who the event is for. `Church` comes next. The other types have roughly equal priority.
* Subtypes have roughly equal priority. When an event fits two, such as a picnic that is a potluck, the one its title or description names foremost wins.
* Priority lives in how examples are labeled. A centroid cannot learn a priority rule, which shows as Youth→Education in leave-one-out. If it wants code, its home is `TagAnnotation.kt`, as a tie-break toward the higher type when scores are close.

## Abstaining

The rule is "prefer missed information to misinformation": an event with no type shows as Event in the UI. No Miscellaneous value is added to the model enums; abstaining lives in the classifier.

* Types: a margin gate, giving no type when the top type beats the runner-up by less than a tuned gap. Tune it on the V37 classifications, after centering.
* Subtypes: a "no subtype" centroid, from the 102 examples without a subtype, living only in the classifier. An event nearest it gets none. This replaces the 0.5 threshold.
* Stage 3: a probability threshold states the rule directly.

## Value Survey

On 2026-10-02 the 2,129 distinct event titles were sampled and counted by keyword, and the navigator refined the result. Types added: `Arts`, `FoodAndDrink`. Subtypes added: `DJSet`, `GameNight`, `Lecture`, `Festival`, `Meditation`, `SportsMatch` (spectator games), `PickupGame` (playing in one), `FoodTruck`. Subtypes are grouped in `EventSubtype` by the type they generally fall under.

Runners-up, for a later pass:

* Types: Outdoors (hikes, bird walks, paddling, group rides; about 110). Health & Fitness, renaming Fitness to take in meditation, is under consideration.
* Subtypes: Tasting (about 29), Group Ride and Group Run (about 30 to 40 each), Story Time (common under Youth).

Music is undercounted by keyword, since most concert titles are only the artist's name.

## Stages

Each stage reuses the vectors of the one before; nothing upstream of the embedding changes.

1. **Cosine against label descriptions.** No examples needed. Kept as the fallback for a value with no examples.
2. **Cosine against centroids** of labeled example vectors. Current.
3. **Logistic regression** trained on the same example vectors, giving a probability per type. Wants about 30 to 50 examples per type, regularization, and class balance (Meetup's 43 would tilt it).

A trained layer is tied to the embedding model. Labeled examples are kept as raw text, so a model change re-embeds them and retrains.

Blending is an option between stages: a value's vector as part label and part centroid, so the label wording steers again, and a value with few examples, such as Volunteer, leans on its words.

## Labels

When labels matter (fallbacks, or blending), a richer label naming a few typical members helps the query land, in this asymmetric query-to-document setup. Distinctness matters more than length: a word shared with a neighbor, such as "performance" in both Music and Arts, blurs them.

## Evaluation Set

`daemon/src/test/resources/event-evaluation.json` holds 307 V37 events in the tag shape (2026-10-03, first pass), annotated against every `EventTag` from the rules in `TagAnnotation.kt`, and held out from the example set. 181 were converted from the earlier type/subtype set (seeded from type and subtype, then reviewed against every tag); 126 were added to top up thin tags. Events too unclear to annotate were skipped. No entry has zero tags yet.

Events carrying each tag:

* 20 or more: Music 71, Meetup 47, Education 43, Concert 42, Arts 36, Health & Fitness 32, Food & Drink 27, Holiday 26, Class 25, Sports 22, Books & Writing 20.
* 10 to 19: Nature 19, Games & Trivia 19, Lecture 18, Kids & Family 18, Church 16, Exercise 16, Sports Match 16, Film 14, Crafting 14, Dance 14, Festival 13, Comedy 13, Networking 13, Theater 12, Volunteer 12, DJ Set 11, Singles 10.
* Under 10, wanting leads: Fundraiser 9, Wellness 9, Watch Party 9, Food Truck 8, LGBTQ 7, Market 7, Hike 7, Pets 6, Tasting 6, Tech 5, Potluck 5, Karaoke 5, Pickup Game 4, Picnic 2, Community Outreach 1, Open Mic 1 (the pool's open mics are mostly copies of one event).
* None in V37: Art Exhibition, Cleanup, Street Performance.

The V37 pool is shared with the example set, and an event is used in only one of them, so the thin tags are thin for both until new leads bring more.

## Example Set

`daemon/src/main/resources/event-examples.json` holds 355 events in the tag shape (2026-10-03, first pass): the 273 type/subtype examples, seeded and reviewed against every tag, and 82 added from the rest of the V37 pool. It shares no event with the evaluation set. Some additions are deliberate negatives: bands whose names hold a tag's word (Snarky Puppy, Campground – By The Campfire, First Church of the Last Days) carry Music alone.

Events carrying each tag:

* 20 or more: Education 67, Meetup 58, Music 56, Class 48, Health & Fitness 46, Arts 44, Food & Drink 32, Concert 31, Kids & Family 29, Holiday 24, Books & Writing 24, Exercise 24, Sports 24, Dance 23, Games & Trivia 22.
* 10 to 19: Comedy 19, Wellness 19, Nature 19, Lecture 18, Church 17, Crafting 15, Sports Match 15, Networking 14, Volunteer 14, Festival 13, DJ Set 13, Theater 12, Hike 12, Film 10, Food Truck 10, Fundraiser 10.
* Under 10, wanting leads: LGBTQ 9, Tasting 9, Pickup Game 8, Watch Party 7, Tech 6, Politics 5, Pets 5, Art Exhibition 4, Market 4, Open Mic 4, Singles 2, Cleanup 1, Karaoke 1, Community Outreach 1.
* None: Picnic, Potluck, Street Performance. A tag with no examples falls back to its description in `TagAnnotation.kt`.

The V37 pool is spent for the thin tags; more examples wait on new leads.

## Next

* Review every example by its full text, keeping only those the description makes clear. Move the meditation-shaped Church examples (Golden Satsang, Prayers for World Peace) to Fitness, or rename that type Health & Fitness.
* Add examples where the space overlaps: Music with bare artist names, Arts that is clearly theater or gallery, Youth events that are classes, Volunteer (only 9). Draw them from the V37 reports, favoring low-margin events, judged by their descriptions.
* Center the vectors, then add the type margin gate and the "no subtype" centroid.
* Define which subtypes each type can show.

## Open Questions

* `EventTable` stores `eventType` and `eventSubtype` by ordinal, and V37 stored values. A value added inside a group of `EventSubtype` now renumbers stored values. The columns want `enumerationByName`, or the enums become append-only.
* A shortened description ends with a "Read more" link, which reaches the embedding.
* An event whose embedding fails is not tracked: it shows only as a gap between `created` and `classified + unclassified`.
