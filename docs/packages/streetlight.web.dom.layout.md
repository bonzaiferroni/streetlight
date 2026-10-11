# Package streetlight.web.dom.layout

## Introduction

The app's layouts built in the browser on `AppendScope` or `ViewScope`, including the forms that take no model.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom`, `koala.dom.layout`, `koala.dom.component` | The view runtime, and koala's layouts and components |
| `streetlight.web.layout` | The stylesheets and server-rendered variants of the layouts here |
| `streetlight.web.dom.component` | The app's components built in the browser |
| `kampfire.model` | `MutableTap` |

## Forms

A form is built from the components in `Form.kt`, each a `ViewScope` extension.

| Component | Holds |
|---|---|
| `formCard(name)` | A group of forms shown together, under a heading on a background |
| `formRow` | Form entries in two columns when there is room, and in one column on a narrow screen |
| `formSection(name)` | One form entry. Its heading does most of the work of saying what the field does |
| `centeredText`, `formBullets` | Information a field needs beyond its heading, inside its `formSection` |
| `formSubmit(label, onClick, messenger, back)` | The submit button, its `messageBox`, and an optional back action |

Form entries go in a `formRow`. An entry that deserves the full width on a wide screen, such as a `markdownEditor`, sits in the `formCard` outside any `formRow`.

Closely related fields share one `formSection`. A `formSection`'s name is short and names the field or fields it holds.

A submit button sits below the content it relates to: a field and its `formSubmit` stack in a column, never side by side in a row.

`formColumn` serves no clear purpose and is not used in new forms.

A field that must be filled calls `flowValid(key, validityTap, contentScope)` on its component, keyed by the edit DTO's `FooProperty` constant, so the field is marked while its key is in the DTO's `validity`.

The form functions for a record live in `FooForm.kt` in `streetlight.web.view`, one per card, each taking the record's editor. The view stacks them and ends with `formSubmit`. A form section that takes no model, such as `linksFormSection`, lives here.

An editor uploads a pending image with `imageEditor.finalizeImage(messenger)` before it sends the edit.

The function that does a submit's work calls `deliverSending()` on its messenger before its API call. `formSubmit` does not track the work itself.

## Layouts

| Layout | Role |
|---|---|
| [`mainBody`, `configBody`, `pageBody`](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/body.kt) | The page of a view: its header, its body section and the footer |
| [`formCard`, `formRow`, `formSection`, `formSubmit`…](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/Form.kt) | The parts forms are built from |
| [`formSectionLegacy`, `formBodyProto`…](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/FormElement.kt) | Form parts of the earlier approach, still used by the event scout |
| [`linksFormSection`](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/linksFormSection.kt) | A form section editing a list of links |
| [`cardOf`](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/cardOf.kt) | The card of a location, linking to it or calling a function |
| [`feedRow`](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/feedRow.kt) | Appends a feed row, as the server-rendered `feedRow` |
| [`dataBlock`](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/dataBlock.kt) | Content built from data requested once the block is viewed |
| [`introSection`](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/introSection.kt) | A section introducing a page, with an optional animation |
| [`taskContent`](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/taskContent.kt) | The content of a task, by its kind |
| [`deltaGrid`, `deltaRow`](../../web/src/jsMain/kotlin/streetlight/web/dom/layout/TextDelta.kt) | The differences between two texts, row by row |
