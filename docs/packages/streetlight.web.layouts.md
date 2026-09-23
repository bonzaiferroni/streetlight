# streetlight.web.layouts

Server-renderable components that lay out content. They are declared in `commonMain` on `FlowContent`.

## Feed Rows

`feedRow` renders any `FeedEntity` as one row. It is built around a post, which displays every property the row has, and it is the row for every other entity type, which display the properties they hold.

A property that depends on the entity type is a `FeedEntity` extension in `FeedEntityProperty.kt`, resolved by a `when` over every entity type.

| Property | Decides |
|---|---|
| `contentRoute` | Where the row links |
| `themeColor` | The color scheme |
| `flair` | The badge |
| `cells` | The entity cells |
| `entityButtonsOf` | The buttons, given whether the row shows the more button |

A new `FeedEntity` type adds a branch to each. An entity with no cells or buttons returns an empty list.

A component that shows an entity takes the `FeedEntity` and reads its content from these extensions. Content that no extension holds, or that differs from what the extension holds, is a parameter.

`isUniverse` is `true` when the row is not inside a galaxy's own feed. The post line then names the galaxy the post belongs to.

## Cells

A cell shows one property of the content in a grid-like structure. `cellGrid` lays out a `List<EntityCell>` followed by a `List<EntityButton>` in one wrapping row, and renders nothing when both are empty. A `mod` passed to `cellGrid` is applied after its own modifiers.

| Type | Holds | Rendered as |
|---|---|---|
| `EntityCell` | An icon, a text value and an optional `Url` | A cell, framed as a link when the `Url` is set |
| `EntityButton` | A block that builds its own element | Inside the buttons cell, which holds every button of the grid |

`cellGrid` declares the content of every entity cell and the buttons cell itself, so every entity is rendered the same way. Every cell and the buttons cell have `MinWidth(16)`.

A cell for a single property, such as `costCell` or `starCell`, is a function in `CellContent.kt` returning an `EntityCell`.

The more button toggles the expanded content of a `feedRow`. `feedRow` passes `showMore = true`, and every other component passes `false`.
