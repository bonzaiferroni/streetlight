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
