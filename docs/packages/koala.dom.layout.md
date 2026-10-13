# Package koala.dom.layout

## Introduction

Layouts built in the browser on `AppendScope` or `ViewScope`, including the blocks that rebuild their content from state.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom` | `ViewScope`, `AppendScope` and the view runtime |
| `koala.layout` | The stylesheets and `configureFoo` functions of the layouts here |
| `koala.dom.component` | The components layouts are built from |
| `kampfire.model` | `Tap`, `MutableTap` and `Store` |
| `web` (Kotlin wrappers) | DOM types |

## Fixed Elements

A `flowBlock` that can contain a `position: fixed` element does not use `Blur`, because a `filter` on an ancestor makes the block, not the viewport, the containing block for a fixed element.

## Selector Blocks

`selectorBlock` draws no container and shows nothing when it has no items. The caller wraps it in `selectorContainer`, together with any content that follows the items, such as a prompt shown while the list is empty.

## Layouts

### Feedback

| Layout | Role |
|---|---|
| [`messageBox`](../../koala/src/jsMain/kotlin/koala/dom/layout/messageBox.kt) | A message in a card styled by its type, or nothing |

### Blocks Built From State

| Layout | Role |
|---|---|
| [`flowBlock`](../../koala/src/jsMain/kotlin/koala/dom/layout/flowBlock.kt) | Rebuilds a child view for each value of a flow |
| [`itemsBlock`, `indexedItemsBlock`](../../koala/src/jsMain/kotlin/koala/dom/layout/itemsBlock.kt) | A child view per item, keeping the views of items that stay |
| [`lazyColumn`](../../koala/src/jsMain/kotlin/koala/dom/layout/lazyColumn.kt) | A scrolling column that applies each change in place |
| [`selectorBlock`, `selectorContainer`](../../koala/src/jsMain/kotlin/koala/dom/layout/selectorBlock.kt) | A column of items selected by click, and its scrolling container |
| [`stageBlock`](../../koala/src/jsMain/kotlin/koala/dom/layout/stageBlock.kt) | Numbered stages of an enum over the content of the current one |

### Panels

| Layout | Role |
|---|---|
| [`tabs`](../../koala/src/jsMain/kotlin/koala/dom/layout/tabs.kt) | Tabs over panels, the selected tab bound to a field when given |
| [`sideTabs`](../../koala/src/jsMain/kotlin/koala/dom/layout/sideTabs.kt) | A column of tabs bound to a field, beside the panel the selected tab shows |
| [`lazyTabs`](../../koala/src/jsMain/kotlin/koala/dom/layout/lazyTabs.kt) | Tabs whose panels are built when first shown |
| [`LazyTabScope`](../../koala/src/jsMain/kotlin/koala/dom/layout/LazyTabScope.kt) | Collects the tabs of a `lazyTabs` block |
