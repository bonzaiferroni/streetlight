# Package koala.layout

## Introduction

Layouts declared on `FlowContent`, which arrange components in markup built without the bundle, and the stylesheets and `Style` objects of every layout, including those whose function lives in `koala.dom.layout`.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | Modifiers and classes |
| `koala.page` | `Id` |
| `koala.interop` | `KoalaFun` for markup that calls the browser |

## Layouts

### Containers

| Layout | Role |
|---|---|
| [`div`](../../koala/src/commonMain/kotlin/koala/layout/div.kt) | A `div` with modifiers applied |
| [`box`](../../koala/src/commonMain/kotlin/koala/layout/box.kt) | Children stacked in one grid cell, over one another |
| [`card`, `cardOf`](../../koala/src/commonMain/kotlin/koala/layout/card.kt) | A container with a card's background and corners |
| [`section`](../../koala/src/commonMain/kotlin/koala/layout/section.kt) | A `section` laid out as a flex column |
| [`spacer`, `centeredHeading`](../../koala/src/commonMain/kotlin/koala/layout/spacer.kt) | An empty element that takes up space, and a heading centered in a row |

### Arrangements

| Layout | Role |
|---|---|
| [`row`](../../koala/src/commonMain/kotlin/koala/layout/row.kt) | A flex row |
| [`column`](../../koala/src/commonMain/kotlin/koala/layout/column.kt) | A flex column |
| [`grid`](../../koala/src/commonMain/kotlin/koala/layout/grid.kt) | A grid of column templates, with another on a narrow screen |
| [`carousel`](../../koala/src/commonMain/kotlin/koala/layout/carousel.kt) | A row that scrolls sideways, snapping to each child |
| [`olist`, `ulist`](../../koala/src/commonMain/kotlin/koala/layout/listing.kt) | Lists laid out along an axis |
| [`listItem`](../../koala/src/commonMain/kotlin/koala/layout/listItem.kt) | An item of a list |

### Panels and Overlays

| Layout | Role |
|---|---|
| [`tabs`](../../koala/src/commonMain/kotlin/koala/layout/tabs.kt) | Tabs over panels, wired by the browser with `initTabs` |
| [`TabScope`](../../koala/src/commonMain/kotlin/koala/layout/TabScope.kt) | Collects the tabs of a `tabs` block |
| [`popover`, `popoverCard`](../../koala/src/commonMain/kotlin/koala/layout/popover.kt) | A popover positioned against an anchor |
| [`DialogStyle`](../../koala/src/commonMain/kotlin/koala/layout/dialog.kt) | The stylesheet of the `dialog` in `koala.dom.layout` |

### Stylesheets of Browser Layouts

| Stylesheet | Layout in `koala.dom.layout` |
|---|---|
| [`FlowBlockStyle`](../../koala/src/commonMain/kotlin/koala/layout/flowBlock.kt) | `flowBlock` |
| [`ItemsBlockStyle`](../../koala/src/commonMain/kotlin/koala/layout/itemsBlock.kt) | `itemsBlock` |
| [`LazyColumnStyle`](../../koala/src/commonMain/kotlin/koala/layout/LazyColumnStyle.kt) | `lazyColumn` |
