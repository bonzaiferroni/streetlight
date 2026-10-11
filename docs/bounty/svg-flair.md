# SVG Flair

Replace the emoji flair of buttons and links with icons from `SvgFile`, and simplify the `btn` grid rules that place it.

## Shape

* `flair` is an `SvgPack?` on `btn`, `button`, `buttonPopover`, `safetyButton` and `navigation`, in both source sets. The component renders its `small` variant, 3 units high. The `btn` row is one line high, so the flair overflows into the padding and the line alone sets the height.
* `addFlair` is gone. A label's flair is the default argument of `flair`, and `flair = null` turns it off.
* The flair is a `span` carrying `IconStyle.Icon` and `ButtonStyle.Flair`, so the stylesheet targets it by class instead of by position.

## Icons

| Emoji | Use | Icon |
|---|---|---|
| 🎟 | "tickets" label | `SvgFile.Ticket` |
| ✍ | "edit" label | `SvgFile.Edit` |
| 🌐 | "website" label | `SvgFile.World` |
| 📅 | "calendar" label | `SvgFile.Calendar` |
| 📼 | youtube.com link | `SvgFile.BrandYoutube` |
| 📖 | en.wikipedia.org link | `SvgFile.Book` |
| 🔗 | any other link | `SvgFile.Link` |
| ☰ | `buttonPopover` default | `SvgFile.Menu` |
| 💡 | "Markdown Hints" popover | `SvgFile.Bulb` |

No new icons are wanted.

## Steps

1. 🍻 Retype `flair` in `navigation`, `button`, `btn` and `buttonPopover` (commonMain), and render it as an icon span.
2. 🍻 Retype `flair` in the jsMain `navigation`, `button` and `safetyButton`.
3. 🍻 Replace the label and domain maps with icons, and fold `addFlair` into the default argument.
4. 🍻 Simplify the `btn` grid rules in `ButtonCss.kt`.
5. 🍻 Update the callers in `web`: `eventPortal`, `EventEditForm`, `createPostMenu`.
6. 🍻 Compile `koala` and `web`.
7. 🍻 Amend `koala.component.md`.

## Decisions

| Decision | Reason |
|---|---|
| `flair` takes an `SvgPack`, not an `Svg` | The component knows its size and picks the variant |
| The flair is a `span`, not the `div` of `icon` | A `button` holds phrasing content only |
| The flair is placed by its class | Positional rules broke when `block` added a child first |
| `createPostMenu` keeps the menu icon | Parity with its ☰ |
| The `btn` row is `1lh` | The flair at 3 units would otherwise grow the `btn` past its pill shape |

## Questions
