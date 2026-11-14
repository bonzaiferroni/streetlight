# Package streetlight.web.shell

## Introduction

The server-rendered content of a route. A shell is declared in `commonMain` so the server renders it on the initial load and the browser consumes it with `routeBlock`.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.web.layout`, `streetlight.web.component` | The app's layouts and components |
| `streetlight.web.layout` | `mainBody` |
| `koala.component`, `koala.layout` | The building blocks and arrangements of markup its parts are made of |
| `koala.page` | `dataIsland` |
| `streetlight.model` | Route content types |

## Shell Content

Content the user needs to see immediately, to understand what the page is, belongs in the shell. Content that can gracefully appear once the bundle has loaded belongs in the view instead.

`shellBox { block }` only runs `block` when there is no server-rendered shell already in the document to adopt. Code inside a shell function is therefore part of the first paint. Code placed beside the `shellBox` call, in the view itself, always runs in the browser, after hydration, whether or not a shell was adopted.

## Shell Functions

A shell is an extension on `FlowContent`, named `fooShell`, in a file of the same name. It takes the route's content type, `FooContent`.

A shell declares no state. State the browser needs is carried in attributes and in the data island.

## Choices in Attributes

A choice the route carries, such as the type on `ConnectRoute`, is rendered for every option. The shell sets the choice as an attribute on an ancestor, and a stylesheet shows the matching option by selecting on that attribute and on the option's own. The route's `screenKey` leaves out the choice, and the view sets the attribute again as the route changes.

## Shape

```kotlin
fun FlowContent.fooShell(content: FooContent) {
    mainBody("fooShell.kt") {
        // body
    }

    dataIsland(FooShell.IslandId, content)
}

object FooShell {
    val IslandId = Id("foo-shell__island")
}
```

A sub-app's shell, `fooAppShell`, builds no `mainBody`, since the app's header and footer are not part of a sub-app. Content a shell shares with a sub-app's shell is `fooBody` in `streetlight.web.layout`, which `fooShell` calls inside `mainBody` and the sub-app's shell calls without it, as `eventBody`.

`mainBody` from `streetlight.web.layout` builds the header, the body `section` and the footer. It takes the shell's file name, which the footer links as the source. The body lambda runs inside the body `section`.

| Part | Holds |
|---|---|
| `mainBody` | The route's content |
| `dataIsland` | The content, after `mainBody` |

## Data Island

`FooShell.IslandId` names the island. The route's view reads it with `routeBlock<FooRoute, FooContent>(FooShell.IslandId)` when the route is the initial one, and fetches the content from the API otherwise.

## Feeds

A `FeedEntity` is rendered with `feedRow` from `streetlight.web.layout`, as described in [`streetlight.web.layout`](../layout/README.md).

## Universe

Content that is not specific to a galaxy is universe content.

## Route Dock

The route dock is not shell content. It is not something a user needs to understand the page, so it lives in the app overlay and is documented in [`streetlight.web.view`](../view/README.md).


## Shells

| Shell | Route content |
|---|---|
| [`homeShell`](homeShell.kt) | The home page: galaxies and the universe feed |
| [`galaxyShell`](galaxyShell.kt) | A galaxy |
| [`starShell`](starShell.kt) | A star |
| [`locationShell`](locationShell.kt) | A location |
| [`locationAppShell`](locationAppShell.kt) | The home of a LocationApp |
| [`locationAppEventShell`](locationAppEventShell.kt) | An event of a LocationApp |
| [`eventShell`](eventShell.kt) | An event |
| [`mediaShell`](mediaShell.kt) | A medium |
| [`offeringShell`](offeringShell.kt) | An offering |
| [`cityShell`](cityShell.kt) | A city |
| [`galaxyFeedShell`](galaxyFeedShell.kt) | The galaxy feed |
| [`cityFeedShell`](cityFeedShell.kt) | The city feed |
| [`siteDocShell`](siteDocShell.kt) | A site doc beside the table of all site docs |
| [`aboutShell`](aboutShell.kt) | The about page |
| [`connectShell`](connectShell.kt) | The connect page: the pitch to each `OnboardType` |
| [`aboutAppShell`](aboutAppShell.kt) | The app's about page and its repositories |
| [`privacyPolicyShell`](privacyPolicyShell.kt) | The privacy policy |
| [`passwordResetForm`](passwordResetForm.kt) | The form that sets a new password, sent by its own script |
