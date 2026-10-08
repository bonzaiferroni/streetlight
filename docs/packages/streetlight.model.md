# Module model

## Introduction

The domain types and API endpoints shared by the server and the web client. Both sides compile the same declarations from `commonMain`.

## Dependencies

| Package | Provides |
|---|---|
| `kampfire.api` | `ApiNode`, the endpoint types, and wire value types such as `Slug` and `Markdown` |
| `kampfire.model` | `Outcome`, `GeoPoint`, and the other shared model types |
| `koala` | `Image`, and the route and content interfaces of `koala.model` |
| `kotlinx.serialization` | Serialization of every type that crosses the wire |

## Naming

| Type | Pattern |
|---|---|
| `Foo` | The DTO of a record, in `streetlight.model.data`, in `Foo.kt` |
| `FooId` | A `@JvmInline value class` over `Uuid` implementing `RecordId`, in `Foo.kt` |
| `FooEdit` | The fields a form sends for `Foo` |
| `FooContent` | The content of a route, implementing `RouteContent` |
| `FooRoute` | A route, in `streetlight.model.ui`, paired with a `Screen` entry |

## Endpoints

`Api` in `Api.kt` is a tree of `ApiNode`s holding one endpoint object per call. The endpoints of one model sit under its node, as `Api.Cities`.

## Data Classes

A DTO lists its id first, then its non-nullable properties, its nullable properties, and its time properties last.

## Entities

`Entity` is a sealed interface for content shown in a feed, a header, or anywhere else an entity appears. Its members name general content: `label`, `sublabel`, `description`, `body`, `image`, `links`. A type maps its own fields onto them, as in `override val sublabel get() = tagline`, and leaves a member it does not hold at its default.

`body` is required. It is the entity's `description` when there is one, and otherwise a summary built from its other properties, such as an address line or when it was added. A view that shows the entity's own description, such as a page header, reads `description`; a view that always wants a line of text reads `body`.

## Edits

A record the user edits has a `FooEdit` DTO holding the fields a form sends, and `Foo.toEdit()` to start an edit from the record. Its `validity` is a `ValidityCheck` of the `FooProperty` keys that are missing.

## Leads

`Lead` is the sealed type of a page the crawler is given to read, with what is known of it before it is fetched: an `EventFeed`, an `EventPage` found in a feed, carrying the `RawEntity` the feed showed, a `LocationLead`, a url submitted as a location's page, or an `EventLead`, a url submitted as one event's page. Its `LeadType` names its kind and is stored by ordinal. A user submits a lead as a `StarLead` through `Api.Stars.CreateLead`; for now only an admin may, and only a location or event lead. A lead may carry its page's html as `content`, sent with the `StarLead`, for the crawler to read in place of a fetch. `EventFeed` is the sealed type of a page the crawler reads events from: a `LocationEventFeed`, a location's own events page, or a `GeneralEventFeed` of local events at many locations. A `GeneralEventFeed` carries its `LeadType`: `EventFeed` for one checked again, `EventScan` for one read once. It is distinct from the paged feeds below. `isExternalOrigin` marks a lead whose page may be a platform's rather than the source's: a `GeneralEventFeed` and an `EventLead` are, a location's feed and a `LocationLead` are not, and an `EventPage` follows its feed.

An `LmSchema` is a class the LM is asked to fill: a `SelectorSchema` of selectors, or a direct schema of values such as `LocationSchema` and `EventSchema`.

A `RawEntity` holds the text of each `ParseProperty` read from a page, not yet parsed. A property is shared by every kind of record that has the same meaning, such as `Name` for an event's title and a location's name. A `RawEntity` is built with `buildRawEntity`, setting each property as `this[property] = value`; a null or blank value is left out. `LocationEdit.mergeLeft` keeps the links of the left edit and adds each link of the right whose label it lacks.

## Opening Hours

`expandAddress` spells out the USPS abbreviations of an address's street line: its suffix (`Ave` to `Avenue`) and its direction (`E` to `East`), before the street name or after it. Every other word is kept as written, so a leading "St." of a name stays, a single-letter street keeps its name ("100 E Street"), and a unit and the part after the first comma are untouched. An expanded address expands to itself. Every stored address is expanded when its location is created or updated, and an address is expanded before it is compared with stored ones.

`osmHoursToSchedule` reads an OpenStreetMap `opening_hours` value into an `HoursSchedule`. A rule it cannot read in full is skipped whole, never half-read, and a value it cannot read at all gives `null`. It never throws.

## Feeds

An `EntityFeed` is paged with an `EntityCursor`. A cursor holds the sort value and `recordId` of the last entity on the page, so it pages any table keyed by a `Uuid`. A feed of posts takes its `recordId` from the post; an event feed takes it from the event.

An `EntityFeed` names its `FeedSource`, the feed its next page is read from: `Posts`, `City`, or `Events`. A feed section reads its heading and the reader of its more button from it.

The home feed is the caller's posts, or for a visitor, the upcoming events from `Api.Events.ReadFeed`. A city feed is the upcoming events of the city, without its locations, from `Api.Cities.ReadFeed`. Both list events soonest first and page from `EntityCursor.Upcoming`.
