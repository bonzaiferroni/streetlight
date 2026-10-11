# Package streetlight.web.pages

## Introduction

Whole HTML documents the server responds with: the head, the app body around a route's shell, and standalone pages.

## Dependencies

| Package | Provides |
|---|---|
| `koala.html` | `appHead`, and the components the body is built from |
| `koala.modifier` | Modifiers and classes |
| `koala.interop` | `HeadScriptConfig` and `RootSwitch` |
| `kotlinx.html` | The HTML DSL |
| `streetlight.web.shells` | The shells placed in the body |

## Pages

A page is an extension on `HTML`. It calls `appHead` with its title, then builds its body. A route's page is `appPage`, which places the route's shell inside `appBody`.

A head support, such as maps or protobuf, is a `HEAD` extension in `supports.kt`, added by the page that needs it.

## Headers

A page header is built on `rayHeader`, which places its content between a ray in `AccentFg` on the left and one in `PrimaryFg` on the right. Content in the header follows the same colors from left to right.

## Head Script

The site's root settings are declared in the `HeadScriptConfig` in `ServerResource.kt`. A new root setting is added there.

## Themes

A theme is a set of custom properties from `PageTheme.toStyleSet()`. The viewer's default theme is the root's inline style, stored under `DEFAULT_THEME_KEY` and restored by the head script. A page's own theme is the body's inline style and overrides the default; clearing the body returns the page to the default.

`DefaultThemeOnly` is a root modifier. While it is on, `PageThemeCss` makes the body inherit every `KoalaStyle` property from the root, over any page theme.
