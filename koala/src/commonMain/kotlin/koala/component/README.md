# Package koala.component

## Introduction

Components declared on `FlowContent`, which build markup without the bundle, and the stylesheets and `Style` objects of every component, including those whose function lives in `koala.dom.component`.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | Modifiers and classes |
| `koala.page` | `Id` |
| `koala.interop` | `KoalaFun` for markup that calls the browser |
| `kampfire.model` | Shared model types |

## Interactive Components

An interactive component, such as `textField` or `searchField`, has two variants under one name: a `FlowContent` function here for markup rendered without the bundle, and a `ViewScope` function in `koala.dom.component` that takes a `MutableTap` and binds the element to it. The structure and modifiers both share are set by `configureFoo` functions here, one per element, which both variants call; the `koala.dom.component` variant adds only the binding.

## Images

An image component takes the `Image`, not a URL of one size, so it renders the image's source set. A new image component configures its `img` with `configureImage`.

A caller changes the fit of a `containImage` on its root, which the content image inherits.

## Flair

A button's or link's flair is an `SvgPack`, rendered as its `small` variant in a span carrying `ButtonStyle.Flair`. The `btn` stylesheet places the flair by that class, never by child position. A `btn` row is one line high, and the flair overflows into its padding, so the flair never sets the height. A `btn` defaults its flair from its label, and from its link's domain for an `href`. A caller passes `flair = null` for none.

## Root Switch

`rootSwitch(attribute, mod) { value -> }` renders a row of element buttons, one per value of an enum root setting. The lambda builds each button's content. A button applies its value with `applyRootSwitch` and carries it under `attribute.optionAttribute`.

The app marks the selected button by adding `rootSwitchCss(attribute, values)` to its own stylesheet. The default style sets `color` to `--primary-fg`; a caller passes another style to change it.

The setting is restored on load through the app's `HeadScriptConfig`, described in [`koala.interop`](../interop/README.md).

## Components

### Containers

| Component | Role |
|---|---|
| [`div`](div.kt) | A `div` with modifiers applied |
| [`box`](box.kt) | Children stacked in one grid cell, over one another |
| [`card`, `cardOf`](card.kt) | A container with a card's background and corners |
| [`section`](section.kt) | A `section` laid out as a flex column |
| [`spacer`, `centeredHeading`](spacer.kt) | An empty element that takes up space, and a heading centered in a row |

A container is a `div`. `box` is reserved for children stacked over one another.

### Arrangements

| Component | Role |
|---|---|
| [`row`](row.kt) | A flex row |
| [`column`](column.kt) | A flex column |
| [`grid`](grid.kt) | A grid of column and row templates |
| [`carousel`](carousel.kt) | A row that scrolls sideways, snapping to each child |
| [`olist`, `ulist`](listing.kt) | Lists laid out along an axis |
| [`listItem`](listItem.kt) | An item of a list |

### Overlays

| Component | Role |
|---|---|
| [`popover`, `popoverCard`](popover.kt) | A popover positioned against an anchor |
| [`DialogStyle`](dialog.kt) | The stylesheet of the `dialog` in `koala.dom.component` |

### Buttons and Links

| Component | Role |
|---|---|
| [`btn`](btn.kt) | A link styled as a button |
| [`button`](button.kt) | A button with text, an icon or built content, styled as a `btn` |
| [`buttonPopover`](buttonPopover.kt) | A button that opens a popover |
| [`navigation`](navigation.kt) | A link, with an optional flair before its content |
| [`rootSwitch`](rootSwitch.kt) | A row of buttons, one per value of a root setting |

### Inputs

| Component | Role |
|---|---|
| [`checkBox`](checkBox.kt) | A checkbox with its label |
| [`searchField`](searchField.kt) | A search input in a pill with a search icon |
| [`textField`](textField.kt) | A text input labeled over its corner |
| [`SwitchStyle`](switch.kt) | The stylesheet of the `switch` in `koala.dom.component` |
| [`SwitchMenuStyle`](switchMenu.kt) | The stylesheet of the `switchMenu` in `koala.dom.component` |
| [`FilePickerKey`](filePicker.kt) | The stylesheet of the `filePicker` in `koala.dom.component` |
| [`GlowControlStyle`](GlowControlStyle.kt) | The stylesheet of the `glowField` in `koala.dom.component` |

### Text

| Component | Role |
|---|---|
| [`textBlock`, `heading1`…](typography.kt) | Paragraphs, headings and spans |
| [`textSpan`](textSpan.kt) | Text in a span |
| [`textLabel`](textLabel.kt) | Small, faded text |
| [`textProperty`](propertyValue.kt) | A dimmed property name followed by its value |
| [`sectionHeading`](sectionHeading.kt) | A heading row with its label centered |
| [`filigree`](filigree.kt) | A heading framed between two decorative rules |
| [`bulletsOf`](bulletsOf.kt) | A bulleted list |
| [`wbr`](wbr.kt) | A line break opportunity |
| [`BlockLabelCss`](blockLabel.kt) | The stylesheet of the `blockLabel` in `koala.dom.component` |

### Images and Icons

| Component | Role |
|---|---|
| [`image`](image.kt) | An image with its source set, or a placeholder |
| [`containImage`](featureImage.kt) | A whole image over a blurred backdrop of itself |
| [`fillImage`](fillImage.kt) | An image filling the width over a blurred backdrop of itself |
| [`headerImage`](headerImage.kt) | A 3:2 image under a gradient, with a heading over its bottom edge |
| [`metaImage`](metaImage.kt) | An image with its alt text and attribution |
| [`icon`, `magicIcon`](icon.kt) | An SVG icon in the current color, and its animated variant |
| [`lottie`](lottie.kt) | A Lottie animation, played by the browser |
| [`geoMapMount`](geoMap.kt) | The element the browser mounts a map into |
| [`GeoMapCss`](GeoMapCss.kt) | The stylesheet of the map and its controls |

### Feedback

| Component | Role |
|---|---|
| [`progressBar`](progressBar.kt) | A bar filled to a progress from 0 to 1 |
| [`hr`](hr.kt) | A horizontal rule |
