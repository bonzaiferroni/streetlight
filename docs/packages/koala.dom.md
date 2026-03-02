### Introduction

The `koala.dom` package provides a Kotlin/JS DSL for building and managing the DOM. It extends `kotlinx.html` to provide a set of styled components and utilities that integrate with `koala.css` and `kotlinx.coroutines`. It is primarily used for creating reactive, component-based UIs in the browser.

### Dependencies

- `kotlinx.html`: The base DSL for building HTML structures.
- `kotlinx.coroutines`: Used for managing asynchronous operations and rendering lifecycles.
- `koala.css`: Provides the styling engine and `ModifierSet` used by DOM elements.
- `koala.html`: Provides common HTML-related types like `Id`.

### Structures

#### DOMContext
`DOMContext` is a typealias for `TagConsumer<HTMLElement>`. It serves as the primary context for building HTML elements. Most UI components in this package are defined as extension functions on `DOMContext`.

#### RenderContext
`RenderContext` is an interface that extends `DOMContext` and adds a `renderScope: CoroutineScope`. This allows components to launch coroutines that are tied to the rendering lifecycle.

#### Styled Elements
Common layout elements like `row`, `column`, `box`, and `fullscreenBox` are implemented as extension functions that apply `koala.css` modifiers to standard HTML tags (usually `div`).

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

### Workflows
