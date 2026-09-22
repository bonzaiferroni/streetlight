### Introduction

The `koala.dom` package provides a Kotlin/JS DSL for building and managing the DOM. It extends `kotlinx.html` to provide a set of styled components and utilities that integrate with `koala.modifier` and `kotlinx.coroutines`. It is primarily used for creating reactive, component-based UIs in the browser.

### Dependencies

- `kotlinx.html`: The base DSL for building HTML structures.
- `kotlinx.coroutines`: Used for managing asynchronous operations and rendering lifecycles.
- `koala.modifier`: Provides `Modifier` and `ModifierSet`, applied to DOM elements.
- `koala.html`: Provides common HTML-related types like `Id`.

### Structures

#### DOMContext
`DOMContext` is a typealias for `TagConsumer<HTMLElement>`. It serves as the primary context for building HTML elements. Most UI components in this package are defined as extension functions on `DOMContext`.

#### RenderContext
`RenderContext` is an interface that extends `DOMContext` and adds a `renderScope: CoroutineScope`. This allows components to launch coroutines that are tied to the rendering lifecycle.

#### Styled Elements
Common layout elements like `row`, `column`, `box`, and `fullscreenBox` are implemented as extension functions that apply `koala.modifier` modifiers to standard HTML tags (usually `div`).

```kotlin
inline fun DOMContext.row(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = div {
    applyModifiers(Row, modifiers)
    content()
}
```

#### Mounting and Wiring
Functions like `wireBlock` and `renderRoot` are used to attach Kotlin-managed DOM fragments to existing elements in the document, often based on their `Id`. Some components like `wireBlock` use visibility tracking (`onView`) to delay rendering until the element is visible in the viewport.

#### Fixed Elements

A `flowBlock` that can contain a `position: fixed` element does not use `Blur`, because a `filter` on an ancestor makes the block, not the viewport, the containing block for a fixed element.

#### Route Blocks

`routeBlock` builds a view from the content of its route. It reads the island the shell carried when the route is the initial one, and otherwise takes the content from `RouteInflator`.

`RouteInflator` fetches the content of each new route through a `ContentFetcher`, which maps a route to its endpoint and is implemented by the application. A route on a screen with `hasShell` skips the fetch when it is the initial route. The island is read whenever the route is the initial one, whether or not `hasShell` is set, so a shell screen without the flag works and fetches content it does not use.

### Workflows
