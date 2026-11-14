# Package streetlight.web.page

## Introduction

Whole HTML documents the server responds with: the head, the app body around a route's shell, and standalone pages.

## Dependencies

| Package | Provides |
|---|---|
| `koala.page` | `appHead` |
| `koala.component`, `koala.layout` | The components and layouts the body is built from |
| `koala.modifier` | `modify`, and the classes and styles it applies to elements |
| `koala.interop` | `HeadScriptConfig` and `RootSwitch` |
| `kotlinx.html` | The HTML DSL |
| `streetlight.web.shell` | The shells placed in the body |

## Pages

A page is an extension on `HTML`. It calls `appHead` with its title, then builds its body. A route's page is `appPage`, which places the route's shell inside `appBody`. A sub-app's page, `fooAppPage`, builds its own body around the portal mount and the shell mount, and links the sub-app's bundle.

A page's stylesheet is `buildSiteStyles()`, which `ServerResource` serves and the promo shots add at startup. A new `FooCss` is listed in `KtStyles` in `siteStyles.kt`.

A head support, such as maps or protobuf, is a `HEAD` extension in `supports.kt`, added by the page that needs it.

## Headers

A page header is built on `rayHeader`, which places its content between a ray in `AccentFg` on the left and one in `PrimaryFg` on the right. Content in the header follows the same colors from left to right.

A shell's top bar is `topBar`, given a `TopBar` from `streetlight.model.ui`, the value the app's top bar takes. It is the first child of the shell's single root, since a shell built on the client sits in a `box`, which stacks its children over one another. Its background reaches to the viewport's edges. In `TopBarMode.Ghost`, the default, it scrolls away with the page; in `Sticky` it stays at the top of the viewport over the content.

## Head Script

The site's root settings are declared in the `HeadScriptConfig` in `ServerResource.kt`. A new root setting is added there.

## Themes

A theme is a set of custom properties from `PageTheme.toStyleSet()`. The viewer's default theme is the root's inline style, stored under `DEFAULT_THEME_KEY` and restored by the head script. A page's own theme is the body's inline style and overrides the default; clearing the body returns the page to the default.

`DefaultThemeOnly` is a root modifier. While it is on, `PageThemeCss` makes the body inherit every `KoalaStyle` property from the root, over any page theme.

## App Overlay

`appOverlay` is a fixed layer above the route content. `helmBar` sits at its top and the route dock at its bottom. The overlay passes no pointer events, so a child that takes input sets `pointer-events: auto`, with `PointerEventsAuto` or, when it changes with a state such as the route dock's `Hidden`, in its stylesheet.

## Pages

### Documents

| Page | Role |
|---|---|
| [`appPage`](appPage.kt) | An app page for a screen |
| [`locationAppPage`](locationAppPage.kt) | A page of a LocationApp, with none of the app's menus, overlay or footer |
| [`staticPage`](staticPage.kt) | A page without the client app |
| [`messagePage`](messagePage.kt) | A static page showing a message |
| [`aboutPage`](aboutPage.kt) | The about page |
| [`eventPortal`](eventPortal.kt) | A standalone page for requesting songs at an event |
| [`eventSignUp`](eventSignUp.kt) | A standalone sign-up page for an event |
| [`supportGeoMap`, `supportProtobuf`](supports.kt) | Head supports added by the page that needs them |

### Page Chrome

| Part | Role |
|---|---|
| [`appBody`](appBody.kt) | The body of an app page around the portal mount |
| [`appHeader`, `rayHeader`](appHeader.kt) | The headers of a page |
| [`topBar`](topBar.kt) | The top bar of a page |
| [`appFooter`](appFooter.kt) | The footer of a page |
| [`appOverlay`](appOverlay.kt) | The fixed layer over the page |
| [`helmBar`](helmBar.kt) | The bar of helm buttons |
| [`siteHelmPopover`, `siteMenuSidebar`](siteMenu.kt) | The site menu |
| [`starHelmPopover`](starHelm.kt) | The star menu |
| [`applyTheme`, `toStyleSet`](applyTheme.kt) | Themes, as specified under Themes |
| [`StreetlightCss`](StreetlightCss.kt) | The site-wide stylesheet |
| [`buildSiteStyles`](siteStyles.kt) | The stylesheet every page serves, assembled from the base sheets and `KtStyles` |
| [`AppAttribute`](AppAttribute.kt) | The data attributes the app's scripts read |
| [`PopoverId`](PopoverId.kt) | The ids of the app's popovers |
