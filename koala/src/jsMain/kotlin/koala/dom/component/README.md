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
| [`div`](div.kt) | A `div` with modifiers applied |
| [`box`](box.kt) | Children stacked in one grid cell, over one another |
| [`card`, `cardOf`](card.kt) | A container with a card's background and corners |
| [`section`](section.kt) | A `section` laid out as a flex column |

A container is a `div`. `box` is reserved for children stacked over one another.

### Arrangements

| Component | Role |
|---|---|
| [`row`](row.kt) | A flex row |
| [`column`](column.kt) | A flex column |
| [`grid`](grid.kt) | A grid of column and row templates |

### Overlays

| Component | Role |
|---|---|
| [`dialog`, `dialogContent`, `dialogCard`](dialog.kt) | A modal dialog open while a field is `true`, and its content |
| [`popover`, `popoverCard`](popover.kt) | A popover, with or without a card |
| [`progressBar`](progressBar.kt) | A bar following a progress tap |

### Buttons and Links

| Component | Role |
|---|---|
| [`button`](button.kt) | A button calling a function when clicked, enabled by a flow when given |
| [`safetyButton`](safetyButton.kt) | A button that asks for a second click before it acts |
| [`buttonDialog`](buttonDialog.kt) | A button that opens a dialog |
| [`navigation`](navigation.kt) | A link, with an optional flair before its content |

### Inputs

| Component | Role |
|---|---|
| [`textField`](textField.kt) | A text input bound to a field |
| [`textAreaEditor`](textAreaEditor.kt) | A plain text area bound to a field |
| [`searchField`](searchField.kt) | A search input bound to a field |
| [`checkBox`](checkBox.kt) | A checkbox bound to a field |
| [`switch`](switch.kt) | A labeled on/off switch bound to a field |
| [`dropMenu`](dropMenu.kt) | A select of options bound to a field |
| [`switchMenu`](switchMenu.kt) | A field's options laid out side by side, the selected one lighted; for options that fit where a `dropMenu` would hide them |
| [`datetimeInput`, `dateInput`, `timeInput`](datetimeInput.kt) | Date and time inputs bound to a field |
| [`colorPicker`](colorPicker.kt) | A button showing a color that opens the browser's color picker |
| [`glowField`](glowField.kt) | A field where a handle is dragged to place a background glow |
| [`filePicker`](filePicker.kt) | An area that picks a file by click or drop, and previews an image |
| [`fileDrop`](fileDrop.kt) | A file picker bound to the picked file's URL |
| [`imageDrop`](imageDrop.kt) | An image picker bound to a field |
| [`audioDrop`](audioDrop.kt) | An audio picker bound to a url field, with a player once set |
| [`recordAudio`](recordAudio.kt) | A microphone recorder bound to a url field, with a player once set |

### Text

| Component | Role |
|---|---|
| [`textBlock`, `span`, `em`, `strong`](text.kt) | Paragraphs and spans, a paragraph bound to a tap |
| [`blockLabel`](blockLabel.kt) | A container labeled over its corner |
| [`filigree`](filigree.kt) | A heading framed between two decorative rules |

### Images and Media

| Component | Role |
|---|---|
| [`image`](image.kt) | An image, hidden while there is none |
| [`icon`](icon.kt) | An SVG icon in the current color, fixed or following a tap |
| [`iconToggle`](iconToggle.kt) | A button whose icon shows a boolean tap, pressed while on |
| [`lottie`](lottie.kt) | A Lottie animation, started at once |
| [`geoMapMount`](geoMapMount.kt) | The element a map is mounted into |
| [`lineChart`](lineChart.kt) | A line chart redrawn from a flow |
