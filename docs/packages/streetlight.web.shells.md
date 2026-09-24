# Package streetlight.web.shells

## Introduction

The server-rendered content of a route. A shell is declared in `commonMain` so the server renders it on the initial load and the browser consumes it with `routeBlock`.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.web.layouts` | Components that lay out content |
| `streetlight.web.ui` | `shellBody` |
| `koala.html` | Components and `dataIsland` |
| `streetlight.model` | Route content types |

## Shell Content

Content the user needs to see immediately, to understand what the page is, belongs in the shell. Content that can gracefully appear once the bundle has loaded belongs in the view instead.

`shellBox { block }` only runs `block` when there is no server-rendered shell already in the document to adopt. Code inside a shell function is therefore part of the first paint. Code placed beside the `shellBox` call, in the view itself, always runs in the browser, after hydration, whether or not a shell was adopted.

## Shell Functions

A shell is an extension on `FlowContent`, named `fooShell`, in a file of the same name. It takes the route's content type, `FooContent`.

A shell declares no state. State the browser needs is carried in attributes and in the data island.

## Shape

```kotlin
fun FlowContent.fooShell(content: FooContent) {
    shellBody("fooShell.kt") {
        // body
    }

    dataIsland(FooShell.IslandId, content)
}

object FooShell {
    val IslandId = Id("foo-shell__island")
}
```

`shellBody` from `streetlight.web.ui` builds the header, the body column and the footer. It takes the shell's file name, which the footer links as the source. The body lambda runs inside the body column.

| Part | Holds |
|---|---|
| `shellBody` | The route's content |
| `dataIsland` | The content, after `shellBody` |

## Data Island

`FooShell.IslandId` names the island. The route's view reads it with `routeBlock<FooRoute, FooContent>(FooShell.IslandId)` when the route is the initial one, and fetches the content from the API otherwise.

## Feeds

A `FeedEntity` is rendered with `feedRow` from `streetlight.web.layouts`, as described in `streetlight.web.layouts.md`. A feed of content that is not specific to a galaxy passes `isUniverse = true`.

## Universe

Content that is not specific to a galaxy is universe content.

## Route Dock

The route dock is not shell content. It is not something a user needs to understand the page, so it lives in the app overlay and is documented in `streetlight.web.ui.md`.

