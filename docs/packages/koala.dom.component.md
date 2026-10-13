# Package koala.dom.component

## Introduction

Components built in the browser on `AppendScope` or `ViewScope`, bound to state where they take input.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom` | `ViewScope`, `AppendScope` and the view runtime |
| `koala.component` | The `configureFoo` functions and stylesheets of the components here |
| `koala.dom.layout` | `flowBlock` |
| `kampfire.model` | `MutableTap` and `Tap` |
| `web` (Kotlin wrappers) | DOM types |

## Interactive Components

The `ViewScope` variant of an interactive component builds its element through the `configureFoo` functions of its `koala.component` variant and adds the `MutableTap` binding, as specified in `koala.component`.

## Components

### Containers

| Component | Role |
|---|---|
| [`div`](../../koala/src/jsMain/kotlin/koala/dom/component/div.kt) | A `div` with modifiers applied |
| [`box`](../../koala/src/jsMain/kotlin/koala/dom/component/box.kt) | Children stacked in one grid cell, over one another |
| [`card`, `cardOf`](../../koala/src/jsMain/kotlin/koala/dom/component/card.kt) | A container with a card's background and corners |
| [`section`](../../koala/src/jsMain/kotlin/koala/dom/component/section.kt) | A `section` laid out as a flex column |

### Arrangements

| Component | Role |
|---|---|
| [`row`](../../koala/src/jsMain/kotlin/koala/dom/component/row.kt) | A flex row |
| [`column`](../../koala/src/jsMain/kotlin/koala/dom/component/column.kt) | A flex column |
| [`grid`](../../koala/src/jsMain/kotlin/koala/dom/component/grid.kt) | A grid of column and row templates |

### Overlays

| Component | Role |
|---|---|
| [`dialog`, `dialogContent`, `dialogCard`](../../koala/src/jsMain/kotlin/koala/dom/component/dialog.kt) | A modal dialog open while a field is `true`, and its content |
| [`popover`, `popoverCard`](../../koala/src/jsMain/kotlin/koala/dom/component/popover.kt) | A popover, with or without a card |

### Buttons and Links

| Component | Role |
|---|---|
| [`button`](../../koala/src/jsMain/kotlin/koala/dom/component/button.kt) | A button calling a function when clicked, enabled by a flow when given |
| [`safetyButton`](../../koala/src/jsMain/kotlin/koala/dom/component/safetyButton.kt) | A button that asks for a second click before it acts |
| [`buttonDialog`](../../koala/src/jsMain/kotlin/koala/dom/component/buttonDialog.kt) | A button that opens a dialog |
| [`navigation`](../../koala/src/jsMain/kotlin/koala/dom/component/navigation.kt) | A link, with an optional flair before its content |

### Inputs

| Component | Role |
|---|---|
| [`textField`](../../koala/src/jsMain/kotlin/koala/dom/component/textField.kt) | A text input bound to a field |
| [`textEditor`](../../koala/src/jsMain/kotlin/koala/dom/component/textEditor.kt) | A plain text area bound to a field |
| [`searchField`](../../koala/src/jsMain/kotlin/koala/dom/component/searchField.kt) | A search input bound to a field |
| [`checkBox`](../../koala/src/jsMain/kotlin/koala/dom/component/checkBox.kt) | A checkbox bound to a field |
| [`switch`](../../koala/src/jsMain/kotlin/koala/dom/component/switch.kt) | A labeled on/off switch bound to a field |
| [`dropMenu`](../../koala/src/jsMain/kotlin/koala/dom/component/dropMenu.kt) | A select of options bound to a field |
| [`optionSwitch`](../../koala/src/jsMain/kotlin/koala/dom/component/optionSwitch.kt) | A field's options laid out side by side, the selected one lighted; for options that fit where a `dropMenu` would hide them |
| [`datetimeInput`, `dateInput`, `timeInput`](../../koala/src/jsMain/kotlin/koala/dom/component/datetimeInput.kt) | Date and time inputs bound to a field |
| [`colorPicker`](../../koala/src/jsMain/kotlin/koala/dom/component/colorPicker.kt) | A button showing a color that opens the browser's color picker |
| [`glowField`](../../koala/src/jsMain/kotlin/koala/dom/component/glowField.kt) | A field where a handle is dragged to place a background glow |
| [`filePicker`](../../koala/src/jsMain/kotlin/koala/dom/component/filePicker.kt) | An area that picks a file by click or drop, and previews it |
| [`fileDrop`](../../koala/src/jsMain/kotlin/koala/dom/component/fileDrop.kt) | A file picker bound to the picked file's URL |
| [`imageDrop`](../../koala/src/jsMain/kotlin/koala/dom/component/imageDrop.kt) | An image picker bound to a field |

### Text

| Component | Role |
|---|---|
| [`textBlock`, `span`, `em`, `strong`](../../koala/src/jsMain/kotlin/koala/dom/component/text.kt) | Paragraphs and spans, a paragraph bound to a tap |
| [`blockLabel`](../../koala/src/jsMain/kotlin/koala/dom/component/blockLabel.kt) | A container labeled over its corner |
| [`filigree`](../../koala/src/jsMain/kotlin/koala/dom/component/filigree.kt) | A heading framed between two decorative rules |

### Images and Media

| Component | Role |
|---|---|
| [`image`](../../koala/src/jsMain/kotlin/koala/dom/component/image.kt) | An image, hidden while there is none |
| [`icon`](../../koala/src/jsMain/kotlin/koala/dom/component/icon.kt) | An SVG icon in the current color |
| [`lottie`](../../koala/src/jsMain/kotlin/koala/dom/component/lottie.kt) | A Lottie animation, started at once |
| [`geoMapMount`](../../koala/src/jsMain/kotlin/koala/dom/component/geoMapMount.kt) | The element a map is mounted into |
| [`lineChart`](../../koala/src/jsMain/kotlin/koala/dom/component/lineChart.kt) | A line chart redrawn from a flow |
