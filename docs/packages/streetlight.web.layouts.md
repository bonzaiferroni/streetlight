# Package streetlight.web.layouts

## Introduction

Server-renderable components that lay out content. They are declared in `commonMain` on `FlowContent`.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.html` | Components |
| `koala.modifier` | Modifiers and classes |
| `streetlight.model` | Entities and routes |

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

### Feed Modes

A feed is laid out by `FeedMode`, a site-wide setting held on the root element as `FeedRow.Mode`. A feed opts in with the `FeedRow.Feed` class, which `layoutFeed` sets. Every mode renders the same markup, and the mode selector in `FeedProtoCss` alone places it.

The feed mode is a root switch, as specified in `koala.interop.md`.

The children of `FeedRow.Content` each take a named grid area: `Image`, `Text`, `Badge` and `Cells`. A modifier that differs by mode lives in the CSS, not on the element, including the direction and gap of `PostLine`. A spacing modifier such as `Gap(n)` renders as an inline style, which no stylesheet rule overrides, so a spacing that differs by mode is never set with one.

The table records each mode's intended layout in more detail than a specification usually holds. The modes are finely engineered, and the detail preserves the intent so a later tweak keeps to it. Keep it when amending.

| Mode | Feed | Entry |
|---|---|---|
| `Row` | One column | Thumbnail, centered text and badge in a row, cells below; one row from 960px with the cells as its right half |
| `Grid` | Columns of at least 300px filling the row, 2px apart | Image across the top at 3:2, contained over its backdrop, with no padding, left-aligned text and badge below, then cells. The expanded content and more button are hidden |
| `Minimal` | One column | One row with no padding: an 8-unit square image, left-aligned text, and the flair badge at the same height. The postline is on one line, its parts separated by a non-breaking space. Cells, expanded content and more button are hidden |

In `Grid`, a child of the mount that is not an entry, such as the more button, spans the full width.

An entry shows its image with `containImage`, passing the `Image` itself so it carries its source set. `Row` sets `object-fit: cover`, which fills the square thumbnail and hides the backdrop. `Grid` keeps the `contain` fit.

## Cells

`cellGrid` lays out the cells of an entity, followed by one buttons cell that holds its buttons.

| Type | Holds |
|---|---|
| `EntityCell` | An icon, a text value, an optional `Url` and an optional label. A cell with a `Url` is a link |
| `EntityButton` | A block that builds its own element |

A cell for a single property, such as `costCell`, is a function in `cellGrid.kt` returning an `EntityCell`.

A cell whose text does not name its subject, such as a count, carries a label.
