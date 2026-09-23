# streetlight.web.layouts

Server-renderable components that lay out content. They are declared in `commonMain` on `FlowContent`.

## Entity Properties

A property that depends on the entity type is an `Entity` extension function in `EntityProperty.kt`, resolved by a `when` over every entity type. A new `Entity` type adds a branch to each.

| Form | Used when | Example |
|---|---|---|
| `toFoo()` extension function | The entity alone decides the value | `Entity.toCells()` |
| `fooOf(entity, …)` function | The value also depends on where it is shown | `entityButtonsOf(entity, showMore)` |

A property returns `null` for content the entity does not have, and a parameter that receives it is nullable.

## Components

A component that shows an entity takes the `Entity` and reads its content from the entity properties. Content that no property holds, or that differs from what the property holds, is a parameter.

An entity supplies its content as data, such as an `EntityCell`. The component that shows the content declares its markup, so every entity is rendered the same way.

## Feed Rows

`feedRow` renders any `Entity` as one row. It is built around a post, which displays every property the row has, and it is the row for every other entity type, which display the properties they hold.

`isUniverse` is `true` when the row is not inside a galaxy's own feed. The post line then names the galaxy the post belongs to.

The more button toggles the expanded content of a `feedRow`. Only a component with collapsed content shows it.

`entityBody` renders an entity's description and links. `feedRow` shows it as its expanded content, capped with `limit`, and `entityHeader` shows it in full with the edit route. The links are a grid of equal-width buttons, capped at `--unit-32`: in columns that wrap on a narrow container, and in one column beside the description from 960px.

## Cells

`cellGrid` lays out the cells of an entity, followed by one buttons cell that holds its buttons.

| Type | Holds |
|---|---|
| `EntityCell` | An icon, a text value, an optional `Url` and an optional label. A cell with a `Url` is a link |
| `EntityButton` | A block that builds its own element |

A cell for a single property, such as `costCell`, is a function in `cellGrid.kt` returning an `EntityCell`.

A label follows the text, at `OpacityHigh`. A cell whose text does not name what it shows, such as a count, carries a label.
