# koala.modifier

A `Modifier` is one change to an HTML element. `Modifier` is a sealed interface, and `ModifierSet` holds an array of nullable modifiers while being a `Modifier` itself, so a set nests inside a set.

## Modifier Kinds

| Type | Holds | Lands as |
|---|---|---|
| `ClassModifier` | An identifier | A class name |
| `InlineStyle<T>` | A `Property<T>` and a value | A declaration in the `style` attribute |
| `AttributeValue<T>` | An `Attribute<T>` and a value | An attribute |
| `ModifierSet` | Nullable modifiers | Each member, in order |

A `when` over a `Modifier` names all four and needs no `else`. The `ModifierSet` branch recurses, because a member may itself be a set, and skips nulls.

`Class` carries the identifier alone. `UtilityClass` pairs a `Class` with the CSS text defining it and delegates `ClassModifier` to that class, so either type is accepted wherever a class modifier is.

`Attribute` and `Property` are descriptors; `AttributeValue` and `InlineStyle` are a descriptor bound to a value. `Attribute.to(value)` and `Property.of(value)` are the factories. A descriptor marked custom prefixes its own name — `data-` for an attribute, `--` for a property — so call sites read `identifier` and never `name`.

Shared descriptors live as vals in the `Attribute` companion and in the `Css` object, so each identifier string is written once.

## Common Source

The common source applies modifiers while HTML is being built, on kotlinx.html receivers such as `TagConfig` and `CoreAttributeGroupFacade`, by writing into the `attributes` map.

`addModifiers` appends to the `class` and `style` values already on the tag rather than replacing them, so two callers modifying one tag do not erase each other.

## JS Source

The js source applies modifiers to a live element, on `web.*` receivers. `modify` and `unmodify` are the pair, `isModified` and `contains` are the queries, and `toggle` combines them. Each mutating function returns its receiver so calls chain.

`Element.style` is an inline cast to `HTMLElement`, which lets these functions take the wider `Element` receiver while reaching inline styles.

| Function | Accepts |
|---|---|
| `modify`, `unmodify`, `isModified`, `toggle`, `contains` | Any `Modifier` |
| `trigger`, `modifyAfterFrame`, `unmodifyAfterFrame` | `ClassModifier` |

The second group takes only a class because it schedules the change on a later animation frame, which exists to let a CSS transition observe the class arriving.

`isModified` asks the element; `contains` asks a modifier. Both compare a leaf by identifier and string value rather than by `==`, because `Class` and `UtilityClass` are different types for the same class name, and a `Property` holds a lambda that defeats data-class equality. `contains` is true when every leaf of the argument is found in the receiver, so a `ModifierSet` argument is satisfied only in full, and a null member is skipped.

## Class Utilities

A utility is a `UtilityClass` made by `utilityOf(identifier, declarations...)`, which builds the rule `.identifier { declaration; ... }`. Each declaration is a CSS string.

A class defined in a hand-written stylesheet is declared as a `Class` val, without a definition, in the hybrid file that styles it.

Utilities live in `*UtilityCss.kt` files grouped by concern. Each file starts with a `val FooUtilityCss get() = listOf(...)` that names every utility the file defines, under comments naming the groups, followed by the definitions in the same groups. A utility missing from the list is never written to the stylesheet.

The identifier is the kebab-case form of the val name.

## Stylesheets

A `*Css.kt` file without `Utility` in its name is a hybrid Kotlin and CSS file. It declares each class it styles as a `Class` val at the top, followed by the stylesheet text as a raw string in a `val FooCss get()`, annotated `// language="CSS"`.

```kotlin
val Foo = Class("foo")

// language="CSS"
val FooCss get() = """
$Foo {
    display: flex;
}
"""
```

The stylesheet refers to each class by interpolating the val, as in `$Foo`, and never by writing the selector.

A rule of one or two declarations that fits on one line is written on one line. A family of such rules is written as consecutive lines with no blank line between them, and the property names and values on those lines are aligned in columns.

`KoalaTheme` holds the values interpolated into `ThemeCss`. `Koala` is the default instance.

## Inline Style Utilities

`CssUtility.kt` holds `InlineStyle` vals, each one a `Property` from `Css` bound to a value. Each is named for the property followed by the value, in PascalCase.

A `Property<LinearDimension>` invoked with an `Int` yields a multiple of `--unit`. Invoked with a `LinearDimension` it yields that dimension unchanged.

`Css` lists the standard descriptors first and the custom ones after.

A style that changes a single property value is an `InlineStyle` rather than a `UtilityClass`. A `UtilityClass` remains where the style must not override a class already held by the element.

`Css` defines each property descriptor. `CssUtility.kt` exposes the most frequently used descriptors as package-level `get()` vals, so that `MinHeight(8)` resolves through the `invoke` operators in `InlineStyle.kt`.

An invoker utility is a package-level `get()` val returning a `Css` descriptor, called with the value. A value utility is a `val` bound to a descriptor and a value, such as `AlignItemsCenter`. A value utility keeps its name when it replaces a `UtilityClass`, so call sites are unchanged. An invoker utility replaces a name such as `MinHeight8` with the call `MinHeight(8)`.

A `UtilityClass` that becomes an `InlineStyle` is removed from its `*UtilityCss` list.

Converting a class to an `InlineStyle` is preceded by an analysis of the DOM structure and the stylesheets, for any rule that sets the same property on an element that carries the modifier.

A descriptor for a value that is a plain number is typed `Number` or `Int`. Otherwise it takes the `kotlinx.css` type for the value. Where `kotlinx.css` has no type for the property, the descriptor takes the enum of a related property whose values are all valid for it, or `String` when none fits.

A utility that sets more than one property remains a `UtilityClass`.

A descriptor called rarely has no package-level `get()` val and is invoked through `Css`, as in `Css.Top(1)`.

A value utility for a zero length is written with `0.px`, because the `Int` invoker yields `calc(var(--unit) * 0)`.

A value utility whose name equals a `kotlinx.css` type qualifies that type in its initializer, as in `kotlinx.css.FlexWrap.wrap`.

Modifiers are imported with `koala.modifier.*`, not by name.

A property with few variations in use is defined in `CssUtility.kt` as a fixed `InlineStyle`, such as `Flex1` and `BorderRadius1`. A property whose values are kept consistent across the app is also a fixed `InlineStyle`, such as `Gap2Px`.

## Other Files

| File | Holds |
|---|---|
| `KoalaBody.kt` | Ids and attributes of the body element, and the screen selector generator |
| `ScriptBuilder.kt` | `jsScriptOf`, which assembles JS text from `JsFunction`s |
| `Rgb.kt` | `Rgb` and its hex conversions |
