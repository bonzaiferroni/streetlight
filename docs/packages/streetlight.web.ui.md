# streetlight.web.ui

The browser-side presentation layer. View functions build the DOM through the `koala.dom` DSL and wire it to state exposed by the view models in `streetlight.web.model`.

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

Functions prefixed `wire` attach behavior to an element the server already rendered, rather than building their own subtree.

## Scopes

`ViewScope` is the receiver throughout the package. `View` is its implementation and holds the tree: a view registers disposers with `onDispose`, and disposing a view disposes its children in reverse order.

| Scope | Lives until |
|---|---|
| `scope` | The view is disposed |
| `contentScope` | The view's content is cleared or replaced |

Work that rebuilds with the content belongs in `contentScope`. Work that must survive a content rebuild belongs in `scope`. `launchEffect` uses the first and `launchViewEffect` the second.

A view model constructed inside a view is given `contentScope`, so its coroutines end when the content it serves is replaced.

## Screens and Routes

`Screen` is an enum in `streetlight.model.ui`. Each entry holds a `RouteParse` that turns a URL into a `StreetlightRoute`, and an optional path root taken from the entry name when absent.

`viewPortal` renders whatever `Portal` has selected, mapping each `Screen` to its route function in a single `when`. A screen with no branch falls to the catch-all rather than failing.

To add a screen: declare the `Screen` entry and its route, write `viewFoo` and `viewFooRoute`, then add the branch in `viewPortal`.

## Services

`appModule` is the Koin module holding every browser-side service. `AppContainer` wraps the resulting Koin instance and is reached through `ViewScope.app`.

A view model with per-view state is not registered in the module. It is built by a factory extension on `AppContainer` that takes the scope and any starting value, so each view gets its own.

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
