# streetlight.web.shells

The server-rendered content of a route. A shell is declared in `commonMain` so the server renders it on the initial load and the browser consumes it with `routeBlock`.

## Shell Functions

A shell is an extension on `FlowContent`, named `fooShell`, in a file of the same name. It takes the route's content type, `FooContent`.

A shell declares no state. State the browser needs is carried in attributes and in the data island.

## Shape

```kotlin
fun FlowContent.fooShell(content: FooContent) {
    column(BodyStyle.ShellColumn) {
        appHeader()

        section(BodyStyle.MainColumn) {
            // body
            appFooter()
        }
    }

    dataIsland(FooShell.IslandId, content)
}

object FooShell {
    val IslandId = Id("foo-shell__island")
}
```

| Part | Holds |
|---|---|
| Outer column | `BodyStyle.ShellColumn`, `appHeader`, and the body container |
| Body container | `BodyStyle.MainColumn`, the route's content, and `appFooter` |
| `dataIsland` | The content, after the outer column |

## Data Island

`FooShell.IslandId` names the island. The route's view reads it with `routeBlock<FooRoute, FooContent>(FooShell.IslandId)` when the route is the initial one, and fetches the content from the API otherwise.

Every shell ends its body container with `appFooter`. The body container is a `section`.

## Feeds

A `FeedEntity` is rendered with `feedRow` from `streetlight.web.layouts`, as described in `streetlight.web.layouts.md`. A feed of content that is not specific to a galaxy passes `isUniverse = true`.

## Universe

Content that is not specific to a galaxy is universe content.
