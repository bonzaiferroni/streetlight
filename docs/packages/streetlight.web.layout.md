# Package streetlight.web.layout

## Introduction

Layouts declared on `FlowContent`, which the server renders, and the `Style` objects of the app's layouts in both halves. A form that takes no model is a layout.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.component`, `koala.layout` | Components and layouts |
| `koala.modifier` | Modifiers and classes |
| `streetlight.model` | Entities and routes |
| `streetlight.web.model` | The entity properties |
| `streetlight.web.component` | The app's components |

## Entity Layouts

A layout that shows an entity takes the `Entity` and reads its content from the entity properties. Content that no property holds, or that differs from what the property holds, is a parameter.

An entity supplies its content as data, such as an `EntityDetail`. The component that shows the content declares its markup, so every entity is rendered the same way.

## Entity Header

`entityHeader` renders the header of an `Entity`, reading its content from the entity properties in `streetlight.web.model`. A header for content that is not an `Entity` calls `pageHeader` with each value.

A value the entity holds but the header shows differently is a parameter defaulted to the entity's own, as `cells` and `image`.

A header is laid out by the `HeaderType` of its `HeaderBlock`, which `renderHeader` passes through `headerOf` to `entityHeader`. `Card` is `pageHeader`, `Hero` is `heroHeader` and `Marquee` is `marqueeHeader`; all take the same arguments, so a new layout of the header is one more function of that shape and one more `HeaderType`.

`heroHeader` opens with the image across the body's width, flush with the top of its block. The image dissolves into the page through a mask on the `img`, not an overlay, so the page's theme shows through, and the title sits over the dissolve, lit by the theme's `--rho-color`. The mask is on the `img` alone, so the image's attribution stays visible.

`marqueeHeader` centers the title above the image, as a sign over a window. The image sits in a frame, darkening toward its bottom, over a blurred copy of itself that fades out evenly on every side; the cells follow below the frame.

## Entity Parts

The parts an entity is shown with, shared by `feedRow` and `entityDialog`, are in `entityParts.kt`: `entityImage`, `entityHeading`, `entitySummary`, `entityBadge` and `entityLinks`. Each takes the entity and the classes its caller places it with, so the two layouts render the same markup and differ only in their stylesheets.

The heading links to the entity's source when it has one, and to its page otherwise. The image and the summary open the `EntityDialog` for the entity's `toEntityRef()`; an entity with no ref links its image to its page and leaves its summary inert. A click on a link inside the summary follows the link and does not open the dialog.

## Entity Dialog

`entityDialog` shows all of an entity. Its image, its heading centered on a card background, and its cells join edge to edge as one part, with no badge; its whole body and links follow through `entityBody`, as a page header shows them, the links beside the body when there is room. The image keeps its native size, at most half the viewport's height, so the content below it shows. It has its own structure and stylesheet, `EntityDialogCss`, so the feed row's modes do not reach it.

The feed carries only what a row shows. The dialog reads the whole entity when it opens, through `Api.Entities.Read`, by the `EntityRef` its opener carries as the `EntityDialog` attributes. Location, media and event entities, and the posts of each, have a ref; a post's dialog shows the record it shares.

## Entity Body

`entityBody` shows an entity's whole description and its links, with its own stylesheet, `EntityBodyCss`. Its layout reads the nearest size container, which its host provides: the links sit beside the description from 640px, and above it below that.

## Feed Sections

`feedSection` shows a feed under its heading, the name of its type. A menu of the types its context offers sits left of the heading, the `FeedMode` switch right of it, each the same width so the heading stays centered. The menu shows even for a single type, telling the viewer what the feed holds; the type shown is marked in the primary color. When the caller passes `typeRoute`, each type links to the route that shows it, and the page is read again for it. Each row's cells are read for the feed's context, so an event in a location's own feed leaves out its location. The types offered come from the feed, which the server limits by viewer, as it keeps the posts of followed galaxies from a visitor, and otherwise from the context.

A feed whose type `isSearched` shows a search field under the heading, beside the tag filter of an `Events` feed. A new searchable type adds itself to `isSearched`, and its server read takes the cursor's `search`.

## Feed Rows

`feedRow` renders any `Entity` as one row. It is built around a post, which displays every property the row has, and it is the row for every other entity type, which display the properties they hold.

### Feed Modes

A feed is laid out by `FeedMode`, a site-wide setting held on the root element as `FeedRowStyle.Mode`. A feed opts in with the `FeedRowStyle.Feed` class, which `layoutFeed` sets. Every mode renders the same markup, and the mode selector in `FeedRowCss` alone places it, showing or hiding a part, such as the detail lines only `Minimal` shows.

An entry marked `FeedRowStyle.Featured` takes the `Grid` entry layout in any feed or mode, sharing the `Grid` rules.

An entry's size is set by `--row-height` on `FeedRowStyle.Base`: 10 units, and 8 in `Minimal`. The square image, the flair and the clip of the text all read it, so a mode changes the size of its entries by setting it alone. A mode that shows the summary clips its text at the row height, and a featured entry at 24 units. Text that reaches the clip fades over its last rem; shorter text does not fade. `Minimal` shows no summary and does not clip. Text is left-aligned in every mode, and starts at the top of its area.

An entry's flair is the icon of its tag for an event that has one, and the `FlairIcon` of its record otherwise. The badge shows the `animated` variant, and the `large` variant when the viewer prefers reduced motion.

The feed mode is a root switch, as specified in `koala.interop.md`.

The children of `FeedRowStyle.Content` each take a named grid area: `Image`, `Text`, `Badge` and `Cells`. A property that differs by mode lives in the CSS, not on the element. A spacing modifier such as `Gap(n)` renders as an inline style, which no stylesheet rule overrides, so a spacing that differs by mode is never set with one.

The table records each mode's intended layout in more detail than a specification usually holds. The modes are finely engineered, and the detail preserves the intent so a later tweak keeps to it. Keep it when amending.

| Mode | Feed | Entry |
|---|---|---|
| `Row` | One column | Thumbnail, text and badge in a row, cells below; one row from 960px with the cells as its right half |
| `Grid` | Columns of at least 300px filling the row, 2px apart | Image across the top at 3:2, contained over its backdrop, with no padding, text and badge below, then cells |
| `Minimal` | One column | One row with no padding: an 8-unit square image, then the priority details in their colors, the title on one line, and the other details at high opacity, and the flair badge. The summary and the cells are hidden |

In `Grid`, a child of the mount that is not an entry, such as the more button, spans the full width.

An entry shows its image with `containImage`, passing the `Image` itself so it carries its source set. `Row` sets `object-fit: cover`, which fills the square thumbnail and hides the backdrop. `Grid` keeps the `contain` fit.

## Cells

`cellGrid` lays out the cells of an entity.

The layout is described here so its intent survives later changes. `cellGrid` is a size container split into as many equal columns of at least 128px as fit, up to 7, stepped by `@container` rules that set `--cols`. Each cell takes one column and every row is 5 units tall. The last cell grows to fill the rest of its row. Each cell holds its content in an inner row that fills the cell, so the last cell's content spans the space it grows into. The last cell holds content that can use that space.

| Type | Holds |
|---|---|
| `EntityDetail` (`streetlight.model.data`) | A text after an icon, or after a label in the scheme color, or alone, with an optional `Url`. A cell with a `Url` is a link |
| `EntityButton` | A block that builds its own element |

An entity's cells are its `toDetails()`. A cell shown outside an entity's details, such as `starCell`, is a function in `cellGrid.kt` returning an `EntityDetail`.

## Previews

A preview of an edit, in `postRow.kt`, is a `feedRow` of a `CustomEntity` carrying the edit's `recordType`, which gives it the color and flair of the record. It passes the cells from the builder its record's `toDetails()` branch uses, such as `eventDetailsOf`, and is posted by the signed-in star just now, as other viewers will see it.

## Layouts

### Pages and Headers

| Layout | Role |
|---|---|
| [`mainBody`, `pageBody`](../../web/src/commonMain/kotlin/streetlight/web/layout/body.kt) | The page of a shell: the header, a body section and the footer |
| [`pageHeader`](../../web/src/commonMain/kotlin/streetlight/web/layout/pageHeader.kt) | The header card of a page |
| [`entityHeader`](../../web/src/commonMain/kotlin/streetlight/web/layout/entityHeader.kt) | The `pageHeader` of an entity |
| [`headerOf`](../../web/src/commonMain/kotlin/streetlight/web/layout/headerOf.kt) | The `pageHeader` of a location, galaxy, star or medium |
| [`renderLayout`](../../web/src/commonMain/kotlin/streetlight/web/layout/renderLayout.kt) | The page of content, by its design's layout |

### Entities

| Layout | Role |
|---|---|
| [`entityImage`, `entityHeading`, `entitySummary`, `entityBadge`, `entityLinks`](../../web/src/commonMain/kotlin/streetlight/web/layout/entityParts.kt) | The parts an entity is shown with |
| [`entityDialog`](../../web/src/commonMain/kotlin/streetlight/web/layout/entityDialog.kt) | All of an entity, as the entity dialog shows it |
| [`entityBody`](../../web/src/commonMain/kotlin/streetlight/web/layout/entityBody.kt) | An entity's description and links |
| [`cellGrid`](../../web/src/commonMain/kotlin/streetlight/web/layout/cellGrid.kt) | The facts of an entity as a grid of icon cells |
| [`cardOf`](../../web/src/commonMain/kotlin/streetlight/web/layout/cardOf.kt) | The card of an event, location or galaxy |
| [`smallGalaxyCard`](../../web/src/commonMain/kotlin/streetlight/web/layout/smallGalaxyCard.kt) | A small card of a galaxy |
| [`largePostCard`](../../web/src/commonMain/kotlin/streetlight/web/layout/largePostCard.kt), [`largeEventPostCard`](../../web/src/commonMain/kotlin/streetlight/web/layout/largeEventPostCard.kt), [`largeLocationPostCard`](../../web/src/commonMain/kotlin/streetlight/web/layout/largeLocationPostCard.kt) | A large card of a post |

### Feeds

| Layout | Role |
|---|---|
| [`feedSection`, `layoutFeed`](../../web/src/commonMain/kotlin/streetlight/web/layout/feedSection.kt) | A feed under its heading, with its mode switch, tag filter and search |
| [`feedRow`](../../web/src/commonMain/kotlin/streetlight/web/layout/FeedRow.kt) | An entity in a feed, laid out by the `FeedMode` |
| [`postRow`](../../web/src/commonMain/kotlin/streetlight/web/layout/postRow.kt) | A preview of the feed row an edit will post as |
| [`layoutPostListing`](../../web/src/commonMain/kotlin/streetlight/web/layout/layoutGalaxyListing.kt) | The posts of a galaxy, as one list or a tab for each type |
| [`layoutEventPosts`](../../web/src/commonMain/kotlin/streetlight/web/layout/layoutEventPosts.kt) | Event posts under a heading |
| [`layoutLocationPosts`](../../web/src/commonMain/kotlin/streetlight/web/layout/layoutLocationPosts.kt) | Location posts under a heading |
| [`layoutTalkPreview`](../../web/src/commonMain/kotlin/streetlight/web/layout/layoutTalkPreview.kt) | The comments of a route as threads |

### Stylesheets

| Stylesheet | Styles |
|---|---|
| [`LayoutStyle`](../../web/src/commonMain/kotlin/streetlight/web/layout/LayoutStyle.kt) | The blocks of `renderLayout` and the layout builder |
| [`TextDeltaStyle`](../../web/src/commonMain/kotlin/streetlight/web/layout/TextDelta.kt) | The `deltaGrid` in `streetlight.web.dom.layout` |
