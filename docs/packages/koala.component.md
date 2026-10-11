# Package koala.component

## Introduction

Components declared on `FlowContent`, which build markup without the bundle, and the stylesheets and `Style` objects of every component, including those whose function lives in `koala.dom.component`.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | Modifiers and classes |
| `koala.layout` | The containers components are built from |
| `koala.page` | `Id` |
| `koala.interop` | `KoalaFun` for markup that calls the browser |
| `kampfire.model` | Shared model types |

## Interactive Components

An interactive component, such as `textField` or `searchField`, has two variants under one name: a `FlowContent` function here for markup rendered without the bundle, and a `ViewScope` function in `koala.dom.component` that takes a `MutableTap` and binds the element to it. The structure and modifiers both share are set by `configureFoo` functions here, one per element, which both variants call; the `koala.dom.component` variant adds only the binding.

## Images

An image component takes the `Image`, not a URL of one size, so it renders the image's source set. A new image component configures its `img` with `configureImage`.

A caller changes the fit of a `containImage` on its root, which the content image inherits.

## Root Switch

`rootSwitch(attribute, mod) { value -> }` renders a row of element buttons, one per value of an enum root setting. The lambda builds each button's content. A button applies its value with `applyRootSwitch` and carries it under `attribute.optionAttribute`.

The app marks the selected button by adding `rootSwitchCss(attribute, values)` to its own stylesheet. The default style sets `color` to `--primary-fg`; a caller passes another style to change it.

The setting is restored on load through the app's `HeadScriptConfig`, described in `koala.interop.md`.

## Components

### Buttons and Links

| Component | Role |
|---|---|
| [`btn`](../../koala/src/commonMain/kotlin/koala/component/btn.kt) | A link styled as a button |
| [`button`](../../koala/src/commonMain/kotlin/koala/component/button.kt) | A button with text, an icon or built content, styled as a `btn` |
| [`buttonPopover`](../../koala/src/commonMain/kotlin/koala/component/buttonPopover.kt) | A button that opens a popover |
| [`navigation`](../../koala/src/commonMain/kotlin/koala/component/navigation.kt) | A link, with an optional flair before its content |
| [`rootSwitch`](../../koala/src/commonMain/kotlin/koala/component/rootSwitch.kt) | A row of buttons, one per value of a root setting |

### Inputs

| Component | Role |
|---|---|
| [`checkBox`](../../koala/src/commonMain/kotlin/koala/component/checkBox.kt) | A checkbox with its label |
| [`searchField`](../../koala/src/commonMain/kotlin/koala/component/searchField.kt) | A search input in a pill with a search icon |
| [`textField`](../../koala/src/commonMain/kotlin/koala/component/textField.kt) | A text input labeled over its corner |
| [`SwitchStyle`](../../koala/src/commonMain/kotlin/koala/component/switch.kt) | The stylesheet of the `switch` in `koala.dom.component` |
| [`OptionSwitchStyle`](../../koala/src/commonMain/kotlin/koala/component/optionSwitch.kt) | The stylesheet of the `optionSwitch` in `koala.dom.component` |
| [`FilePickerKey`](../../koala/src/commonMain/kotlin/koala/component/filePicker.kt) | The stylesheet of the `filePicker` in `koala.dom.component` |
| [`GlowControlStyle`](../../koala/src/commonMain/kotlin/koala/component/GlowControlStyle.kt) | The stylesheet of the `glowField` in `koala.dom.component` |

### Text

| Component | Role |
|---|---|
| [`textBlock`, `heading1`…](../../koala/src/commonMain/kotlin/koala/component/typography.kt) | Paragraphs, headings and spans |
| [`textSpan`](../../koala/src/commonMain/kotlin/koala/component/textSpan.kt) | Text in a span |
| [`textLabel`](../../koala/src/commonMain/kotlin/koala/component/textLabel.kt) | Small, faded text |
| [`textProperty`](../../koala/src/commonMain/kotlin/koala/component/propertyValue.kt) | A dimmed property name followed by its value |
| [`sectionHeading`](../../koala/src/commonMain/kotlin/koala/component/sectionHeading.kt) | A heading row with its label centered |
| [`filigree`](../../koala/src/commonMain/kotlin/koala/component/filigree.kt) | A heading framed between two decorative rules |
| [`bulletsOf`](../../koala/src/commonMain/kotlin/koala/component/bulletsOf.kt) | A bulleted list |
| [`wbr`](../../koala/src/commonMain/kotlin/koala/component/wbr.kt) | A line break opportunity |
| [`BlockLabelCss`](../../koala/src/commonMain/kotlin/koala/component/blockLabel.kt) | The stylesheet of the `blockLabel` in `koala.dom.component` |

### Images and Icons

| Component | Role |
|---|---|
| [`image`](../../koala/src/commonMain/kotlin/koala/component/image.kt) | An image with its source set, or a placeholder |
| [`containImage`](../../koala/src/commonMain/kotlin/koala/component/featureImage.kt) | A whole image over a blurred backdrop of itself |
| [`fillImage`](../../koala/src/commonMain/kotlin/koala/component/fillImage.kt) | An image filling the width over a blurred backdrop of itself |
| [`headerImage`](../../koala/src/commonMain/kotlin/koala/component/headerImage.kt) | A 3:2 image under a gradient, with a heading over its bottom edge |
| [`metaImage`](../../koala/src/commonMain/kotlin/koala/component/metaImage.kt) | An image with its alt text and attribution |
| [`icon`, `magicIcon`](../../koala/src/commonMain/kotlin/koala/component/icon.kt) | An SVG icon in the current color, and its animated variant |
| [`lottie`](../../koala/src/commonMain/kotlin/koala/component/lottie.kt) | A Lottie animation, played by the browser |
| [`geoMapMount`](../../koala/src/commonMain/kotlin/koala/component/geoMap.kt) | The element the browser mounts a map into |
| [`GeoMapCss`](../../koala/src/commonMain/kotlin/koala/component/GeoMapCss.kt) | The stylesheet of the map and its controls |

### Feedback

| Component | Role |
|---|---|
| [`progressBar`](../../koala/src/commonMain/kotlin/koala/component/progressBar.kt) | A bar filled to a progress from 0 to 1 |
| [`hr`](../../koala/src/commonMain/kotlin/koala/component/hr.kt) | A horizontal rule |
| [`MessageBox`](../../koala/src/commonMain/kotlin/koala/component/messageBox.kt) | The stylesheet of the `messageBox` in `koala.dom.component` |
