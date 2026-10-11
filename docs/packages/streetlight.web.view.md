# Package streetlight.web.view

## Introduction

Views: compositions tied to a model, which build the DOM through the `koala.dom` packages and bind it to the state of the view models in `streetlight.web.model`. A part of a view that takes a model lives here beside it; a part that takes none is a component or layout. The common half holds the `Style` objects and stylesheets of views, which the server renders.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom` | `ViewScope` and the view runtime |
| `koala.dom.component`, `koala.dom.layout` | Components and layouts built in the browser |
| `koala.modifier` | Modifiers and classes |
| `streetlight.web.model` | View models |
| `streetlight.web.dom.component`, `streetlight.web.dom.layout` | The app's components and layouts built in the browser |
| `streetlight.web.layout`, `streetlight.web.shell` | Server-renderable layouts and the shells views adopt |
| `streetlight.model` | DTOs and routes |

## Layers

The UI has two layers.

| Layer | Package | Holds |
|---|---|---|
| Declarative UI | `streetlight.web.view` | What is rendered from state |
| View model | `streetlight.web.model` | The concept of the UI, its state, and communication with the API |

A view model knows nothing of HTML elements or how they render.

A view is not required to have a view model. `ViewScope` carries the API, and a view whose state is simple declares it and makes its API calls itself. A view gains a view model when its state or its API traffic outgrows that.

## View Functions

A view function is an extension on `ViewScope`, named for what it renders, in a file of the same name.

```kotlin
fun ViewScope.viewFeedbackHub()
```

It takes no context parameter. `ViewScope` carries the `AppContainer`, so a view reaches a service with `app.get<T>()` rather than receiving it through its signature. A new service becomes available to every view at once, and a view's parameters are only what that view is about.

A view rendered directly by a route has a second function taking `RouteScope`, named with a `Route` suffix. The route function reads route arguments and calls the plain view function, so the view itself stays reachable from anywhere.

```kotlin
fun RouteScope.viewFrontDeskRoute() {
    viewFrontDesk()
}
```

Functions prefixed `wire` attach behavior to an element the server already rendered, rather than building their own subtree. They live in `streetlight.web.interop`.

## Scopes

`ViewScope` is the receiver throughout the package. `View` is its implementation and holds the tree: a view registers disposers with `onDispose`, and disposing a view disposes its children in reverse order.

| Scope | Lives until |
|---|---|
| `scope` | The view is disposed |
| `contentScope` | The view's content is cleared or replaced |

Work that rebuilds with the content belongs in `contentScope`. Work that must survive a content rebuild belongs in `scope`. `launchEffect` uses the first and `launchViewEffect` the second.

A view model constructed inside a view is given `contentScope`, so its coroutines end when the content it serves is replaced.

## Rendering State

`flowBlock` renders a value as HTML. It takes a `Tap` or a `Flow`, and its block is rebuilt as a child view whenever the value changes. The previous child view is disposed first.

A state parameter is a `Tap` for a read and a `MutableTap` for a read and write. A `Tap` is a lens onto another `Tap` or onto a `Store`, and a `Store` wraps a `StateFlow`. These types are declared in `kampfire.model`.

## Screens and Routes

`Screen` is an enum in `streetlight.model.ui`. Each entry holds a `RouteParse` that turns a URL into a `StreetlightRoute`, and an optional path root taken from the entry name when absent. A `StaticParse` or `SlugParse` block has the query's `Parameters` as its receiver, so a route reads a query value as `StaticParse { HomeRoute(readFeedType()) }`.

A route is an object while it has no parameters, and a data class once it has any. A route whose page holds a feed with more than one type is a `FeedRoute`: its `feed` names the type chosen, written to its path as the `feed` query parameter and left out for the default, so choosing a type navigates and the backstack holds each choice. Its content endpoint takes the type, and the server's render reads it from the query. The route dock treats a feed route as its `defaultFeedRoute`, so the dock's route stays marked whichever feed it shows.

Portal keeps a screen's view across a change to a route with the same `screenKey`, which is the route itself by default, and rebuilds it on any other change or a refresh. Going to the route already shown counts as a refresh. A feed route's key is its `defaultFeedRoute`, so its view stays, and the view follows the new feed with `followFeedRoute`, which reads the route's content again and swaps the feed section in place. Every Earth route shares `Screen.Earth` as its key, since the Earth view handles each of its routes itself.

`Portal` (`koala.model`) holds the current route as `PortalState`. It builds the route from the address bar on load and on browser navigation, and it intercepts clicks on local anchors. Code that holds a reference to `Portal` navigates with `Portal.go(route)`. With `appendToBackstack = false`, the route takes the place of the current one in the backstack and in the browser's history.

`viewPortal` is a `flowBlock` over `Portal.screenState`. It renders the selected `Screen` inside a `RouteScope` built from the current `PortalState`, mapping each `Screen` to its route function in a single `when`. A screen with no branch falls to the catch-all rather than failing.

To add a screen: declare the `Screen` entry and its route, write `viewFoo` and `viewFooRoute`, then add the branch in `viewPortal`.

A route whose view reads content has a branch in `AppContentFetcher`. The content type implements `RouteContent`, which `AppContentFetcher` and `routeBlock` both require, whether it is a `FooContent` or a record such as `City`.

## Shell Views

A screen that the server renders on the initial load has a shell in `streetlight.web.shell`. Its view is built from the shell.

```kotlin
fun ViewScope.viewFoo(content: FooContent) {
    shellBox {
        fooShell(content)
    }

    document.setTitle(FooRoute)
    applyTheme(null)
}

fun RouteScope.viewFooRoute() {
    routeBlock<FooRoute, FooContent>(FooShell.IslandId) { content ->
        viewFoo(content)
    }
}
```

`shellBox` adopts the server-rendered shell when it is present and builds the shell in the browser when it is not. The view builds no elements of its own.

A shell view is wired through these parts.

| Part | Location |
|---|---|
| `Api.Content.Foo` endpoint | `streetlight.model.Api` |
| `readFooContent` | `streetlight.server.model`, and bound in `serveContent` |
| `readFooContent` on `ContentClient` | `streetlight.web.io` |
| `is FooRoute` branch | `AppContentFetcher` |
| `Screen.Foo` with `hasShell = true` | `streetlight.model.ui.Screen` |
| `Screen.Foo` branch | `viewPortal` |

A shell view with a map uses `shellBoxWithMap` and sets the marker points from the content.

## Page Body

A routed view without a shell builds its page with `mainBody`, passing its file name, the same way a shell does in `streetlight.web.shell.md`.

```kotlin
fun ViewScope.viewFoo() {
    mainBody("viewFoo.kt") {
        pageHeader("Foo", "a subtitle", SiteImage.Foo)
        // body
    }
}
```

A view that configures or creates a record, named `viewFooConfig` or `viewFooScout`, builds its page with `configBody` instead. Its `configHeader` takes the place of the app header and links home, and the view shows no `topLogo` and no heading of its own that names the page.

```kotlin
configBody("Foo", "Config", "viewFooConfig.kt") {
    configHeading(foo.name, foo.route)
    // body
}
```

The body opens with `configHeading`, naming the record the view configures and linking to its page. A scout names the galaxy it posts to. A view with no record, such as the site config, has no heading.

The two title parts render as one word, `titleFirst` in `AccentFg` and `titleSecond` in `PrimaryFg`.

`mainBody` and `configBody` differ only in their header. Both are built on `pageBody`, which holds the shape they share. A new kind of body is built on `pageBody` too.

Screens outside the base layout, such as the sandbox, inbox and earth, use neither.

## Delivering UI Messages

Two messengers deliver text to the user. Both implement `Messenger`, so a view model takes either without knowing which it holds.

| Messenger | Holds | Appears |
|---|---|---|
| `MessageStore` | One `UIMessage?`, replaced on each delivery | Wherever the view places its `messageBox` |
| `Toaster` | A list of messages, each expiring on its own | The global toast area |

Prefer a `MessageStore` when the view has room beside the control the user is operating and the declarative UI is within reach. A message next to the button that caused it needs nothing to explain what it refers to.

Reach for the `Toaster` when there is no room beside the control, or when the message reports work the user did not just start.

### The Two Beats

An interaction that reaches the server delivers twice.

| Beat | Says | Delivered by |
|---|---|---|
| First | The interaction had an effect | `messenger.deliverSending()` |
| Second | This was the result | `toDataOr(messenger, defaultOkMessage)` |

`toDataOr` covers both sides of the second beat. On `Ok` it delivers the outcome's own message, or `defaultOkMessage` when the outcome carries none. On `Problem` it delivers the problem message. A flow that wants no success text still reports a failure, so an interaction is never silent about a problem.

Both beats go to the same messenger. A `MessageStore` holds one message, so the second replaces the first in place and the user reads a single slot from "Sending..." to its result.

An interaction whose effect is invisible needs the second beat most. Feedback submitted privately does not appear in the public feed, and without a message the user has no way to tell a successful send from a silent failure.

### Wiring

The view owns the `MessageStore`. It creates one, passes it to `formSubmit` so the box is rendered next to the button, and hands the same instance to the view model when the work happens there.

```kotlin
val messenger = MessageStore()
formSubmit("Send", { model.sendFeedback(messenger) }, messenger)
```

A view model receives the store as a `Messenger` parameter rather than holding one of its own. The view then decides where a message appears and the view model decides only what it says.

## Route Dock

The route dock holds the routes directly relevant to the route now. It is rendered once by `viewRouteDock` into `AppOverlay.RouteDockId`, wired from `viewApp`, and persists between routes.

| Part | Holds |
|---|---|
| Title | A label naming what the routes belong to |
| Main routes | Route labels as text |
| Left and right routes | Icons, chosen by `iconOf` |

A side route is for a route that not every view wants, such as a config route. A side route with no entry in `iconOf` fails.

The dock holds routes only. An action belongs in the view it acts on.

A dock route equal to the route now carries `RouteDockStyle.RouteNow`, applied with `flowModifier` over `Portal.routeState`. The match is by route, not screen, because routes of one dock can share a screen. It updates in place, so a dock that persists across routes is not rebuilt to move the indicator.

### State

`RouteDock` in `streetlight.web.model` holds the dock's state, reached through `AppFacade.dock`. On every route it sets its state from `stateOf(route)`, which states everything the route alone determines. A route with no branch clears the dock.

A view adds what only its content knows with `dock.mergeState`, naming its own route. A merge replaces each field it sets and leaves the rest.

```kotlin
dock.mergeState(content.galaxy.toRoute(), RouteDockState(title = content.galaxy.name, rightRoutes = rightRoutes))
```

A merge is applied when the state on hand was built from the same route, and held as pending otherwise, to be applied when that route arrives. The test is the route the state was built from, not the current route in `Portal`, which already reads as the new one while the dock's own reaction is still pending. A pending merge is dropped when a different route arrives.

The route passed is the view's own, never `Portal.stateNow.route`, which would name the route that replaced it.

### Visibility

A view hides the dock with `dock.setVisible(false)`, for content it would otherwise sit over. Every route that lands makes it visible again, so a view states only when to hide it and never when to restore it. The reaction that drives it belongs to the view's `contentScope`, so it ends with the view.

Visibility is not part of `RouteDockState`. That state is the dock's content, handed over once per route, while visibility toggles as often as the view's own content does.

The hidden dock carries `RouteDockStyle.Hidden`, applied with `flowModifier`.

Routes of one group share one `stateOf` branch, or a function it calls, so their docks stay consistent. The universe routes, Home, Galaxies and Cities, share `universeStateOf`, whose right route is the earth route that is the cousin of the route now.

## Views

### App

| View | Role |
|---|---|
| [`viewApp`](../../web/src/jsMain/kotlin/streetlight/web/view/viewApp.kt) | The root view: the portal, the overlay and the wired menus |
| [`viewPortal`](../../web/src/jsMain/kotlin/streetlight/web/view/viewPortal.kt) | The view of the current screen |
| [`viewRouteDock, wireRouteDock`](../../web/src/jsMain/kotlin/streetlight/web/view/viewRouteDock.kt) | The route dock |

### Screens

| View | Role |
|---|---|
| [`viewHome`](../../web/src/jsMain/kotlin/streetlight/web/view/viewHome.kt) | The Home screen |
| [`viewGalaxy`](../../web/src/jsMain/kotlin/streetlight/web/view/viewGalaxy.kt) | The Galaxy screen |
| [`viewGalaxyList`](../../web/src/jsMain/kotlin/streetlight/web/view/viewGalaxyList.kt) | The GalaxyList screen |
| [`viewStar`](../../web/src/jsMain/kotlin/streetlight/web/view/viewStar.kt) | The Star screen |
| [`viewLocation`](../../web/src/jsMain/kotlin/streetlight/web/view/viewLocation.kt) | The Location screen |
| [`viewEvent`](../../web/src/jsMain/kotlin/streetlight/web/view/viewEvent.kt) | The Event screen |
| [`viewMedia`](../../web/src/jsMain/kotlin/streetlight/web/view/viewMedia.kt) | The Media screen |
| [`viewCity`](../../web/src/jsMain/kotlin/streetlight/web/view/viewCity.kt) | The City screen |
| [`viewCityList`](../../web/src/jsMain/kotlin/streetlight/web/view/viewCityList.kt) | The CityList screen |
| [`viewSiteDoc`](../../web/src/jsMain/kotlin/streetlight/web/view/viewSiteDoc.kt) | The SiteDoc screen |
| [`viewAboutApp`](../../web/src/jsMain/kotlin/streetlight/web/view/viewAboutApp.kt) | The AboutApp screen |
| [`viewTalkLog`](../../web/src/jsMain/kotlin/streetlight/web/view/viewTalkLog.kt) | The TalkLog screen |
| [`viewInbox`](../../web/src/jsMain/kotlin/streetlight/web/view/viewInbox.kt) | The Inbox screen |
| [`viewChatRoom`](../../web/src/jsMain/kotlin/streetlight/web/view/viewChatRoom.kt) | The ChatRoom screen |
| [`viewEarth`](../../web/src/jsMain/kotlin/streetlight/web/view/viewEarth.kt) | The Earth screen |
| [`viewTransitMap`](../../web/src/jsMain/kotlin/streetlight/web/view/viewTransitMap.kt) | The TransitMap screen |

### Configuration and Editing

| View | Role |
|---|---|
| [`viewGalaxyConfig`](../../web/src/jsMain/kotlin/streetlight/web/view/viewGalaxyConfig.kt) | Configures a galaxy |
| [`viewGalaxyFoundry`](../../web/src/jsMain/kotlin/streetlight/web/view/viewGalaxyFoundry.kt) | Creates a galaxy |
| [`viewLocationConfig`](../../web/src/jsMain/kotlin/streetlight/web/view/viewLocationConfig.kt) | Configures a location |
| [`viewLocationUpdater`](../../web/src/jsMain/kotlin/streetlight/web/view/viewLocationUpdater.kt) | Updates a location |
| [`viewEventUpdater`](../../web/src/jsMain/kotlin/streetlight/web/view/viewEventUpdater.kt) | Updates an event |
| [`viewCityConfig`](../../web/src/jsMain/kotlin/streetlight/web/view/viewCityConfig.kt) | Configures a city |
| [`viewMediaForge`](../../web/src/jsMain/kotlin/streetlight/web/view/viewMediaForge.kt) | Creates media |
| [`viewMediaConfig`](../../web/src/jsMain/kotlin/streetlight/web/view/viewMediaConfig.kt) | Configures a medium |
| [`viewProfileConfig`](../../web/src/jsMain/kotlin/streetlight/web/view/viewProfileConfig.kt) | Configures the star's profile |
| [`viewStarConfig`](../../web/src/jsMain/kotlin/streetlight/web/view/viewStarConfig.kt) | Configures the star's account |
| [`viewSiteConfig`](../../web/src/jsMain/kotlin/streetlight/web/view/viewSiteConfig.kt) | Configures the viewer's site settings |
| [`viewEditHistory`](../../web/src/jsMain/kotlin/streetlight/web/view/viewEditHistory.kt) | The edits of a record |

### Scouts

| View | Role |
|---|---|
| [`viewEventScout`](../../web/src/jsMain/kotlin/streetlight/web/view/viewEventScout.kt) | Posts an event |
| [`viewLocationScout`](../../web/src/jsMain/kotlin/streetlight/web/view/viewLocationScout.kt) | Posts a location |

### Stars and Tools

| View | Role |
|---|---|
| [`viewStarDash`](../../web/src/jsMain/kotlin/streetlight/web/view/viewStarDash.kt) | The star's dashboard |
| [`viewFrontDesk`](../../web/src/jsMain/kotlin/streetlight/web/view/viewFrontDesk.kt) | The front desk |
| [`viewFeedbackHub`](../../web/src/jsMain/kotlin/streetlight/web/view/viewFeedbackHub.kt) | Feedback |
| [`viewBugReporter`](../../web/src/jsMain/kotlin/streetlight/web/view/viewBugReporter.kt) | Bug reports |
| [`viewMusicianHub`](../../web/src/jsMain/kotlin/streetlight/web/view/viewMusicianHub.kt) | A musician's songs |
| [`viewSongProfile`](../../web/src/jsMain/kotlin/streetlight/web/view/viewSongProfile.kt) | A song |
| [`viewTalent`](../../web/src/jsMain/kotlin/streetlight/web/view/viewTalent.kt) | Talent |
| [`viewSiteMonitor`](../../web/src/jsMain/kotlin/streetlight/web/view/viewSiteMonitor.kt) | The site's status |
| [`viewSandbox`](../../web/src/jsMain/kotlin/streetlight/web/view/viewSandbox.kt) | The sandbox |

### Parts of Views

| Part | Role |
|---|---|
| [`signInForm…`](../../web/src/jsMain/kotlin/streetlight/web/view/signInForm.kt) | Signing in |
| [`guestRegistrationForm, fullRegistrationForm`](../../web/src/jsMain/kotlin/streetlight/web/view/registrationForm.kt) | Registering an account |
| [`starGate`](../../web/src/jsMain/kotlin/streetlight/web/view/starGate.kt) | Content for the signed-in star, or a sign-in prompt |
| [`starRouteBlock`](../../web/src/jsMain/kotlin/streetlight/web/view/starRouteBlock.kt) | A `routeBlock` behind a `starGate` |
| [`starConfigTabs`](../../web/src/jsMain/kotlin/streetlight/web/view/starConfigTabs.kt) | The tabs of the star's account config |
| [`emailFormSection`](../../web/src/jsMain/kotlin/streetlight/web/view/emailFormSection.kt) | The email field of a form |
| [`passwordFormSection`](../../web/src/jsMain/kotlin/streetlight/web/view/passwordFormSection.kt) | The password field of a form |
| [`starProfileForm`](../../web/src/jsMain/kotlin/streetlight/web/view/StarForm.kt) | The star's profile |
| [`galaxyNameFormRow…`](../../web/src/jsMain/kotlin/streetlight/web/view/GalaxyForm.kt) | The fields of a galaxy |
| [`cityDetailsForm, cityLinksForm`](../../web/src/jsMain/kotlin/streetlight/web/view/CityForm.kt) | The fields of a city |
| [`locationEditFormBody…`](../../web/src/jsMain/kotlin/streetlight/web/view/LocationEditForm.kt) | The fields of a location |
| [`locationSettingsForm`](../../web/src/jsMain/kotlin/streetlight/web/view/locationSettingsForm.kt) | A location's settings |
| [`locationAutomationForm`](../../web/src/jsMain/kotlin/streetlight/web/view/locationAutomationForm.kt) | A location's event schema |
| [`locationFinder`](../../web/src/jsMain/kotlin/streetlight/web/view/LocationScoutForm.kt) | Finding a location to post |
| [`eventEditFormBody…`](../../web/src/jsMain/kotlin/streetlight/web/view/EventEditForm.kt) | The fields of an event |
| [`eventLinksForm`](../../web/src/jsMain/kotlin/streetlight/web/view/eventLinksForm.kt) | The links of an event |
| [`eventSearchForm`](../../web/src/jsMain/kotlin/streetlight/web/view/EventScoutForm.kt) | Searching for an event to post |
| [`mediaForm`](../../web/src/jsMain/kotlin/streetlight/web/view/MediaForm.kt) | The fields of media |
| [`leadForm`](../../web/src/jsMain/kotlin/streetlight/web/view/LeadForm.kt) | A url sent for the crawler to read |
| [`imageFormSection`](../../web/src/jsMain/kotlin/streetlight/web/view/FormSection.kt) | The image field of a form |
| [`themeForm`](../../web/src/jsMain/kotlin/streetlight/web/view/themeForm.kt) | A page theme |
| [`layoutBuilder`](../../web/src/jsMain/kotlin/streetlight/web/view/layoutBuilder.kt) | The editor of a page layout |
| [`blockBuilder`](../../web/src/jsMain/kotlin/streetlight/web/view/blockBuilder.kt) | The editor of a layout block |
| [`blockMenu`](../../web/src/jsMain/kotlin/streetlight/web/view/blockMenu.kt) | The menu of blocks to add |
| [`CommentView`](../../web/src/jsMain/kotlin/streetlight/web/view/CommentView.kt) | A comment with its replies and forms |
| [`followFeedRoute`](../../web/src/jsMain/kotlin/streetlight/web/view/followFeedRoute.kt) | Swaps the feed section when the route changes its feed |
| [`earthControls`](../../web/src/jsMain/kotlin/streetlight/web/view/earthControls.kt) | The controls of the map |
| [`earthFocus`](../../web/src/jsMain/kotlin/streetlight/web/view/earthFocus.kt) | The panel of the focused marker |
| [`earthMenu`](../../web/src/jsMain/kotlin/streetlight/web/view/earthMenu.kt) | The menu of the map |
| [`earthUnboundedOverlay`](../../web/src/jsMain/kotlin/streetlight/web/view/earthUnboundedOverlay.kt) | Hints toward markers outside the view |
| [`editorSandbox`](../../web/src/jsMain/kotlin/streetlight/web/view/editorSandbox.kt) | The markdown editor's sandbox |
| [`loadingMessages`](../../web/src/jsMain/kotlin/streetlight/web/view/content.kt) | Messages shown while content loads |

### Stylesheets

| Stylesheet | Role |
|---|---|
| [`EarthStyle`](../../web/src/commonMain/kotlin/streetlight/web/view/EarthStyle.kt) | The map screens |
| [`InboxStyle`](../../web/src/commonMain/kotlin/streetlight/web/view/InboxStyle.kt) | The inbox |
| [`LayoutBuilderStyle`](../../web/src/commonMain/kotlin/streetlight/web/view/LayoutBuilderStyle.kt) | The layout builder |
| [`RouteDockStyle`](../../web/src/commonMain/kotlin/streetlight/web/view/RouteDockCss.kt) | The route dock |
| [`CommentClass`](../../web/src/commonMain/kotlin/streetlight/web/view/TalkLogCss.kt) | The talk log |
