# CSS Style

How CSS is written here, beyond what the package documents specify for their own packages. Where each stylesheet lives and how it is served is specified in `docs/packages/koala.modifier.md`.

## Properties

A rule sets the properties that do the job, and no more. A property that changes nothing on the element is left out.

## Stylesheets in Kotlin

A `*Css.kt` file is a hybrid Kotlin and CSS file. It declares each class it styles as a `Class` val at the top, followed by the stylesheet text as a raw string in a `val FooCss get()`, annotated `// language="CSS"`.

```kotlin
val Foo = Class("foo")

// language="CSS"
val FooCss get() = """
$Foo {
    display: flex;
}
"""
```

The stylesheet refers to each class by interpolating the val, as in `$Foo`, and never by writing the selector, except for classes owned by another library. A class declared in an object is interpolated with the getter wrapped in `with(FooObject) { """ ... """ }`.

A comment inside the stylesheet string is a CSS comment, `/* ... */`, never a Kotlin `//` comment.

## Layout of Rules

A rule of one or two declarations that fits on one line is written on one line. A family of such rules is written as consecutive lines with no blank line between them, and the property names and values on those lines are aligned in columns.

```css
.small  { gap: 2px;  }
.medium { gap: 8px;  }
.large  { gap: 16px; }
```

## Modifiers and Stylesheets

A stylesheet holds the presentation unique to a component, usually layout that must come as a package. A property that a modifier can set on the element is set with the modifier, unless it changes with presentation declared in a stylesheet, such as a mode or a container query.

## Sizing Children

A container sizes its direct children through a child rule in its stylesheet, as `CellGrid.Base` does with `> * { flex: 1; min-width: var(--unit-16); }`. An element is not wrapped only to size it.

A flex item sized by its container has an explicit `min-width`.

A default that a modifier on the child may override is written with the container in `:where()`, as `:where($Container) > input`, so the rule has no more specificity than its element selector and a modifier's class wins.

## Inline Styles

Converting a class to an inline style is preceded by an analysis of the DOM structure and the stylesheets, for any rule that sets the same property on an element that carries the modifier.
