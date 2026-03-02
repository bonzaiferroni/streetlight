### Introduction

The `koala.css` package provides a type-safe way to define and apply CSS classes within Kotlin HTML builders. Historically, this package used the `kotlinx.css` DSL to generate style rules dynamically. Currently, the project is migrating to static CSS files (located in `www/css/`) while retaining the `Modifier` interface for type-safe class application.

### Dependencies

* `kotlinx.css`: For CSS DSL and type definitions (primarily used for legacy styles and theme calculations).
* `kotlinx.html`: For applying modifiers to HTML elements.

### Structures

#### Modifier

The core of the package is the `Modifier` interface:

```kotlin
interface Modifier {
    val value: String
    val selector get() = ".$value"
}
```

Most CSS classes are defined as singleton objects implementing this interface:

```kotlin
object AlignItemsCenter : Modifier { override val value = "align-items-center" }
```

#### Css Value Class

A `@JvmInline value class Css(override val value: String) : Modifier` is used for ad-hoc or dynamic class names that aren't predefined objects.

#### ModifierSet

A `typealias ModifierSet = Set<Modifier>` is used when multiple modifiers need to be passed together. The `modify()` function helps create these sets.

#### Application

Modifiers are applied using extension functions on `CoreAttributeGroupFacade`:

* `applyModifiers(modifier: Modifier)`
* `applyModifiers(modifiers: ModifierSet?)`

### Workflows

#### CreateCssUtility(Foo)

* Create an object in the package `koala.css` that implements `Modifier`. The name should be `Foo`.
* The `value` of the `Modifier` should be a kebab-case version of `Foo`.
* Add the corresponding CSS rule to the appropriate static CSS file in `www/css/` (e.g., `utilities.css`, `styles.css`, or `animation.css`).
* Reference the new object in `koala.css.utilities` if it's a general-purpose utility.
