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

## Route Menus

A route menu is declared in the shell as the last child of the outer column, after the body container.

Related routes share one menu function, `fooRouteMenu`, so their menus stay consistent. The function takes the route now and whatever else its group varies by, and calls `routeMenu` from `koala.html`. A shell calls it, and so does a browser-only view of the same group, from inside a block on `ViewScope`.

| Group | Function |
|---|---|
| Star | `starRouteMenu` |
| Universe | `universeRouteMenu` |

`universeRouteMenu` holds Home, Galaxies and Cities. The right tray holds the earth route that is the cousin of the route now: `CityMapRoute` for the city list, `GalaxyMapRoute` for the galaxy list, and `PostMapRoute` for Home.

