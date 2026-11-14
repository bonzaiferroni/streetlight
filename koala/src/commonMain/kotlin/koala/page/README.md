# Package koala.page

## Introduction

The frame a page is built on, without the bundle: its head, scripts, element ids, mounts and data islands.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | `modify`, and the classes and styles it applies to elements |
| `koala.component` | The containers mounts are built from |
| `kotlinx.serialization` | The JSON of data islands |

## Unsafe Content

JSON written into a page through `unsafe { }` is passed through `sanitizeJson()` first, as `dataIsland` does. User text inside unescaped JSON can end the enclosing `script` element.

## Page Frame

| Part | Role |
|---|---|
| [`appHead`](appHead.kt) | The head of an app page, and the links to its files |
| [`scriptUnsafe`](script.kt) | A script written without escaping |
| [`dataIsland`](dataIsland.kt) | Data serialized into the page for the browser to read |
| [`setJsonData`, `setData`](TagUtility.kt) | An attribute holding data as JSON |
| [`mount`](mount.kt) | An empty element for the browser to mount a view into |
| [`Id`, `Queryable`](Id.kt) | The id of an element, and anything a query selects by |
| [`OverlayId`](Ids.kt) | The ids the overlay mounts into |
| [`TagConfig`](types.kt) | A tag that modifiers are applied to while HTML is built |
