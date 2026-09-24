# Package koala.dom

## Introduction

The browser-side DSL that builds and manages the DOM: views, blocks that render state, and route blocks.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL the builders extend |
| `koala.modifier` | Modifiers applied to elements |
| `koala.html` | `Id` and the components shared with server rendering |
| `koala.model` | `Portal` and the route types |
| `kampfire.model` | `Tap`, `Store` and `Messenger` |
| `web` (Kotlin wrappers) | DOM types |
| `kotlinx.coroutines` | View lifecycles |

## Fixed Elements

A `flowBlock` that can contain a `position: fixed` element does not use `Blur`, because a `filter` on an ancestor makes the block, not the viewport, the containing block for a fixed element.

## Route Blocks

`routeBlock` builds a view from the content of its route. It reads the island the shell carried when the route is the initial one, and otherwise takes the content from `RouteInflator`.

`RouteInflator` fetches the content of each new route through a `ContentFetcher`, which maps a route to its endpoint and is implemented by the application. A route on a screen with `hasShell` skips the fetch when it is the initial route. The island is read whenever the route is the initial one, whether or not `hasShell` is set, so a shell screen without the flag works and fetches content it does not use.
