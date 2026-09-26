# Noun Phrase Openings

A description opens with a noun phrase whose head names what it describes. A wh-clause opening ("How…", "What…", "Where…", "When…", "Why…") leaves the subject unnamed. The rule is stated for KDoc in `docs/AGENTS.md`, and covers package documents and root documents alike.

The rule applies to descriptions: a KDoc summary, a document's opening line, a list item or table cell that describes a thing. A conditional sentence ("When a convention is decided, record it…") states a condition, not a description, and is left as it is. A Boolean's KDoc keeps "Whether".

The search that finds candidates:

```
grep -rnE "^\s*(/\*\*|\*)\s+(How|What|Where|Why|When)\b" --include='*.kt' .
grep -nE "^(\s*[-*|]\s*)?(How|What|Where|Why|When)\b" docs/*.md docs/packages/*.md
```

Bounty:
* `docs/package-documentation.md`: widen the style row "No 'What' openers" to cover every wh-clause opening in a description, matching the KDoc rule
* `docs/testing.md:3`: "How tests are written in this project."
* `docs/package-documentation.md:14`: "Where a thing belongs when there are two options"
* `SeedPolicy.kt:92`: "Where a policy applies…"
* `TransitStop.kt:62`: "When a trip reaches a stop."
* `ParseRequest.kt:49`: "What a language model read from a page…"
* `GalaxyMark.kt:29`: "How far a mark leans a post…"
* `GalaxyMark.kt:46`: "How a galaxy's marks are placed…"
* `SiteStatus.kt:56`: "How a metric is gathered…"
* `BaseTask.kt:82`: "What a task view shows…"
* `LocationConfig.kt:26`: "How much of a location's events page is read…"
* `RepeatInterval.kt:7`: "How often a recurring event repeats."
* `ImageBlock.kt:28`: "How an image fills its frame…"
* `NotationStyle.kt:3`: "How pitches are written…"
* `TransitVehicle.kt:20`: "Where a vehicle is relative to its next stop."
* `Rendition.kt:34`: "How a user rates their own rendition."
* `EarthMap.kt:6`: "What the earth view shows…"
* Rerun the KDoc search across `web`, `koala`, `kampfire`, `klutch` and `kabinet`, which were not swept, and add what it finds
* Read each package document for wh-openings the search misses, such as one mid-paragraph or inside a table row
