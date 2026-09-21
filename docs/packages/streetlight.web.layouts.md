# streetlight.web.layouts

Server-renderable components that lay out content. They are declared in `commonMain` on `FlowContent`.

## Feed Rows

`feedRow` renders any `FeedEntity` as one row. It is built around a post, which displays every property the row has, and it is the row for every other entity type, which display the properties they hold.

A property that depends on the entity type is a `FeedEntity` extension in `PostProperty.kt`, resolved by a `when` over every entity type.

| Property | Decides |
|---|---|
| `contentRoute` | Where the row links |
| `themeColor` | The color scheme |
| `flair` | The badge |
| `getCells` | The cells, or `null` for none |

A new `FeedEntity` type adds a branch to each.

`isUniverse` is `true` when the row is not inside a galaxy's own feed. The post line then names the galaxy the post belongs to.

## Cells

A cell shows one property of the content in a grid-like structure. It has an icon and a text value. The buttons cell has no icon or text and holds a row of buttons for interacting with the content.

Cells are built in `cellContentOf.kt` and laid out by `cellBlock`.
