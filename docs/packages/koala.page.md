# Package koala.page

## Introduction

The frame a page is built on, without the bundle: its head, scripts, element ids, mounts and data islands.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | Modifiers and classes |
| `koala.layout` | The containers mounts are built from |
| `kotlinx.serialization` | The JSON of data islands |

## Unsafe Content

JSON written into a page through `unsafe { }` is passed through `sanitizeJson()` first, as `dataIsland` does. User text inside unescaped JSON can end the enclosing `script` element.

## Page Frame

| Part | Role |
|---|---|
| [`appHead`](../../koala/src/commonMain/kotlin/koala/page/appHead.kt) | The head of an app page, and the links to its files |
| [`scriptUnsafe`](../../koala/src/commonMain/kotlin/koala/page/script.kt) | A script written without escaping |
| [`dataIsland`](../../koala/src/commonMain/kotlin/koala/page/dataIsland.kt) | Data serialized into the page for the browser to read |
| [`setJsonData`, `setData`](../../koala/src/commonMain/kotlin/koala/page/TagUtility.kt) | An attribute holding data as JSON |
| [`mount`](../../koala/src/commonMain/kotlin/koala/page/mount.kt) | An empty element for the browser to mount a view into |
| [`Id`, `Queryable`](../../koala/src/commonMain/kotlin/koala/page/Id.kt) | The id of an element, and anything a query selects by |
| [`OverlayId`](../../koala/src/commonMain/kotlin/koala/page/Ids.kt) | The ids the overlay mounts into |
| [`TagConfig`](../../koala/src/commonMain/kotlin/koala/page/types.kt) | A tag that modifiers are applied to while HTML is built |
