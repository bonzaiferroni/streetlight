# Package streetlight.web.layouts

## Introduction

Server-renderable components that lay out content. They are declared in `commonMain` on `FlowContent`.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.html` | Components |
| `koala.modifier` | Modifiers and classes |
| `streetlight.model` | Entities and routes |

## Entity Properties

A property that depends on the entity type is an `Entity` extension function in `EntityUtility.kt`, resolved by a `when` over every entity type. A new `Entity` type adds a branch to each.

| Form | Used when | Example |
|---|---|---|
| `toFoo()` extension function | The entity alone decides the value | `Entity.toCells()` |
| `fooOf(entity, …)` function | The value also depends on where it is shown | `entityButtonsOf(entity, showMore)` |

A property returns `null` for content the entity does not have, and a parameter that receives it is nullable.

## Components

A component that shows an entity takes the `Entity` and reads its content from the entity properties. Content that no property holds, or that differs from what the property holds, is a parameter.

An entity supplies its content as data, such as an `EntityCell`. The component that shows the content declares its markup, so every entity is rendered the same way.

## Entity Parts

The parts an entity is shown with, shared by `feedRow` and `entityDialog`, are in `entityParts.kt`: `entityImage`, `entityHeading`, `entitySummary`, `entityBadge` and `entityLinks`. Each takes the entity and the classes its caller places it with, so the two layouts render the same markup and differ only in their stylesheets.

The heading links to the entity's source when it has one, and to its page otherwise. The image and the summary open the `EntityDialog` for the entity's `toEntityRef()`; an entity with no ref links its image to its page and leaves its summary inert. A click on a link inside the summary follows the link and does not open the dialog.

## Entity Dialog

`entityDialog` shows all of an entity. Its image, its heading centered on a card background, and its cells join edge to edge as one part, with no badge; its whole body and links follow through `entityBody`, as a page header shows them, the links beside the body when there is room. The image keeps its native size, at most half the viewport's height, so the content below it shows. It has its own structure and stylesheet, `EntityDialogCss`, so the feed row's modes do not reach it.

The feed carries only what a row shows. The dialog reads the whole entity when it opens, through `Api.Entities.Read`, by the `EntityRef` its opener carries as the `EntityDialog` attributes. Location, media and event entities, and the posts of each, have a ref; a post's dialog shows the record it shares.

## Feed Sections

`feedSection` shows a feed under its heading, the name of its type. A menu of the types its context offers sits left of the heading, the `FeedMode` switch right of it, each the same width so the heading stays centered. The menu shows even for a single type, telling the viewer what the feed holds; the type shown is marked in the primary color. When the caller passes `typeRoute`, each type links to the route that shows it, and the page is read again for it. Each row's cells are read for the feed's context, so an event in a location's own feed leaves out its location. The types offered come from the feed, which the server limits by viewer, as it keeps the posts of followed galaxies from a visitor, and otherwise from the context.

## Feed Rows

`feedRow` renders any `Entity` as one row. It is built around a post, which displays every property the row has, and it is the row for every other entity type, which display the properties they hold.

The more button toggles the expanded content of a `feedRow`. Only a component with collapsed content shows it.

### Feed Modes

A feed is laid out by `FeedMode`, a site-wide setting held on the root element as `FeedRowStyle.Mode`. A feed opts in with the `FeedRowStyle.Feed` class, which `layoutFeed` sets. Every mode renders the same markup, and the mode selector in `FeedRowCss` alone places it.

An entry marked `FeedRowStyle.Featured` takes the `Grid` entry layout in any feed or mode, sharing the `Grid` rules.

An entry's size is set by `--row-height` on `FeedRowStyle.Base`: 10 units, and 8 in `Minimal`. The square image, the flair and the clip of the text all read it, so a mode changes the size of its entries by setting it alone. A featured entry clips its text at 24 units. Text that reaches the clip fades over its last rem; shorter text does not fade. Text is left-aligned in every mode, and starts at the top of its area.

An entry's flair is the icon of its tag for an event that has one, and the `FlairIcon` of its record otherwise. The badge shows the `animated` variant, and the `large` variant when the viewer prefers reduced motion.

The feed mode is a root switch, as specified in `koala.interop.md`.

The children of `FeedRowStyle.Content` each take a named grid area: `Image`, `Text`, `Badge` and `Cells`. A property that differs by mode lives in the CSS, not on the element. A spacing modifier such as `Gap(n)` renders as an inline style, which no stylesheet rule overrides, so a spacing that differs by mode is never set with one.

The table records each mode's intended layout in more detail than a specification usually holds. The modes are finely engineered, and the detail preserves the intent so a later tweak keeps to it. Keep it when amending.

| Mode | Feed | Entry |
|---|---|---|
| `Row` | One column | Thumbnail, text and badge in a row, cells below; one row from 960px with the cells as its right half |
| `Grid` | Columns of at least 300px filling the row, 2px apart | Image across the top at 3:2, contained over its backdrop, with no padding, text and badge below, then cells. The expanded content and more button are hidden |
| `Minimal` | One column | One row with no padding: an 8-unit square image, text clipped to the same height, and the flair badge. Cells, expanded content and more button are hidden |

In `Grid`, a child of the mount that is not an entry, such as the more button, spans the full width.

An entry shows its image with `containImage`, passing the `Image` itself so it carries its source set. `Row` sets `object-fit: cover`, which fills the square thumbnail and hides the backdrop. `Grid` keeps the `contain` fit.

## Cells

`cellGrid` lays out the cells of an entity.

The layout is described here so its intent survives later changes. `cellGrid` is a size container split into as many equal columns of at least 128px as fit, up to 7, stepped by `@container` rules that set `--cols`. Each cell takes one column and every row is 5 units tall. The last cell grows to fill the rest of its row. Each cell holds its content in an inner row that fills the cell, so the last cell's content spans the space it grows into. The last cell holds content that can use that space.

| Type | Holds |
|---|---|
| `EntityCell` | An icon, a text value, an optional `Url` and an optional label. A cell with a `Url` is a link |
| `EntityButton` | A block that builds its own element |

A cell for a single property, such as `costCell`, is a function in `cellGrid.kt` returning an `EntityCell`.

A cell whose text does not name its subject, such as a count, carries a label.

## Previews

A preview of an edit, in `postRow.kt`, is a `feedRow` of a `CustomEntity` carrying the edit's `recordType`, which gives it the color and flair of the record. It passes the cells from the builder its record's `toCells()` branch uses, such as `eventCells`, and is posted by the signed-in star just now, as other viewers will see it.
