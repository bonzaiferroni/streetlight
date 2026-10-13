# Package streetlight.web.interop

## Introduction

The app's calls between markup and the bundle: the global functions markup calls, and the `wire` functions that bind behavior to elements the server rendered. Each `wire` function is called once from the root view in `viewApp`.

## Dependencies

| Package | Provides |
|---|---|
| `koala.interop` | `JsSignature`, `KtFunction` and `wireBlock` |
| `koala.dom`, `koala.dom.component`, `koala.dom.layout` | The view runtime, `dialog`, `popover`, `flowBlock` and `lazyTabs` |
| `streetlight.web.component`, `streetlight.web.layout` | The markup the functions here find and bind |
| `streetlight.web.model` | Services and view models |

## Interop From Markup

Markup rendered without the bundle calls into it through a global function: a `JsSignature` named in the markup's object, as `FeedSection.MorePosts`, set as an event handler with `invokeJs(ThisElement)`, and a `KtFunction` of the same signature registered in `appGlobalFunctions`. The function receives the element and reads what it needs from it and its closest attributes. A text field passes its input the same way, as an `onInput` handler; the function reads the element's `value` and debounces on its own, as `searchFeed` does.

## Popover Menus

A popover menu is one popover shared by every button that opens it, and is built in two parts.

| Part | Source set | Does |
|---|---|---|
| Button, as `FlowContent.postMenu` | `commonMain` | Targets the menu's `PopoverId` with `setPopoverTarget` and sets the menu's data on itself as attributes |
| Wire, as `ViewScope.wirePostMenu` | `jsMain` | Calls `popoverMenu` with the id, a transform that reads the attributes of the button that opened it into a value, and the content built for that value |

An object named for the menu, as `PostMenu`, holds its `PopoverId` and the attributes its button sets. The wire is called once from the root view in `viewApp`. Content that calls the server reaches it through the `ViewScope`'s `api`, inside a `launchEffect`. A menu whose content needs no data passes a transform that returns `Unit`. A menu opened from inside a feed section, as `TagFilterMenu`, passes the invoker as its value and reads the section's `FeedSource` from it rather than setting its own; `filterFeedByTag` does this, and serves the menu and the dom-agnostic clear button alike through `clearFeedTag`. A menu shows its state as an attribute on the element that displays it, and its stylesheet shows it with `attr()` in the `content` of a pseudo-element and selects on the attribute's presence, as `TagFilterMenu.Tag` does on the tag button.

## Dialogs

A dialog is built with `dialog`, open while a `MutableTap<Boolean>` is `true`, and wired once from the root view in `viewApp` by a `ViewScope.wireFooDialog`. A private store holds what the dialog shows, and the dialog is open while it is set. A function sets it to open the dialog, called from the browser as `startMessage` is, or from markup as a global function, as `openEntityDialog` is. Content that calls the server reads it inside the dialog's content, with `launchEffect`, and renders it with `flowBlock`. A dialog closes itself on a new route, through its state, so a view sets nothing for it.

## Interop

| Part | Role |
|---|---|
| [`appGlobalFunctions`](../../web/src/jsMain/kotlin/streetlight/web/interop/appGlobalFunctions.kt) | Registers the app's functions callable from markup |
| [`AppFun`](../../web/src/commonMain/kotlin/streetlight/web/interop/AppFun.kt) | The app's functions callable from markup, beyond those of its components |
| [`sortByMark`, `morePosts`, `filterFeedByTag`, `searchFeed`…](../../web/src/jsMain/kotlin/streetlight/web/interop/FeedInterop.kt) | Feed actions called from markup |
| [`toggleAny`, `toggleGalaxy`](../../web/src/jsMain/kotlin/streetlight/web/interop/ToggleInterop.kt) | Star toggles called from markup |
| [`updateMark`, `applyCurator`…](../../web/src/jsMain/kotlin/streetlight/web/interop/updateMark.kt) | Marks a post from its curator badge |
| [`readIsland`](../../web/src/jsMain/kotlin/streetlight/web/interop/readIsland.kt) | The island the server rendered, when this is the first route |
| [`wireBadge`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireBadge.kt) | The signed-in star's image in the star badge |
| [`wireCreatePost`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireCreatePost.kt) | The create post menu |
| [`wireCuratorMenu`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireCuratorMenu.kt) | The curator menu of a post |
| [`wireEntityDialog`, `openEntityDialog`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireEntityDialog.kt) | The entity dialog |
| [`wireFps`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireFps.kt) | The frame rate readout |
| [`wireGalaxyMenu`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireGalaxyMenu.kt) | The menu of the star's galaxies |
| [`wireMessageDialog`, `startMessage`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireMessageDialog.kt) | The dialog for writing a message |
| [`wireOmni`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireOmni.kt) | The live connection that toasts a star's records |
| [`wirePostMenu`](../../web/src/jsMain/kotlin/streetlight/web/interop/wirePostMenu.kt) | The menu of a post |
| [`wireRightPanel`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireRightPanel.kt) | The right panel |
| [`wireSignInDialog`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireSignInDialog.kt) | The sign-in dialog |
| [`wireStarHelm`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireStarHelm.kt) | Both star helms: the star's menu or the sign-in panel |
| [`wireStarMenu`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireStarMenu.kt) | The menu of a star named on the page |
| [`wireTagFilterMenu`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireTagFilterMenu.kt) | The tag filter menu of a feed |
| [`wireToaster`](../../web/src/jsMain/kotlin/streetlight/web/interop/wireToaster.kt) | The toasts |
