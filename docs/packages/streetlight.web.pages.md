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

The site's root switches are declared in `StreetlightHeadScript` in `appPage.kt`. A new root switch is added there.
