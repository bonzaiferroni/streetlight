# CellGrid Work

The rework of `cellGrid` (`web/src/commonMain/kotlin/streetlight/web/layouts/cellGrid.kt`) from evenly sized flex cells to a regular column grid, 2026-09-30 to 2026-10-01. It shows in every `feedRow` mode and in `entityHeader`. The layout spec lives in `docs/packages/streetlight.web.layouts.md` under Cells.

## Where It Stands

* `.cell-grid` is a size container (`container-type: inline-size`), a wrapping flex row with a 2px gap.
* Columns: as many as fit at 128px or more, up to 7. Plain `@container` steps set `--cols` at `130k - 2px` (258px, 388px … 908px), so the gap is folded in. Each child is `--cell-width` wide: `calc((100cqi - (var(--cols) - 1) * 2px) / var(--cols))`.
* Every child is `calc(var(--unit) * 5)` (40px) tall, so a line holding only the buttons cell matches the rest.
* `> :last-child` grows to fill the rest of its line, whether it is the buttons cell or an `EntityCell`, so no gap is left without a background.
* Each cell is an outer box (`CellMod` = `CardBg`, plus the link when the cell has a url) holding an inner row (`ContentMod`) that is `--cell-width` wide (`> * > *`). A cell's inner row sits on the left. The buttons cell (`CellGrid.Buttons`) uses `JustifyContentEnd`, so its buttons sit on the right in a space one cell wide, with `SpaceAround`, `Padding(1)` and `Gap(2)`.
* `TextMod` carries `Flex1, TextAlignCenter`: the text is centred in the space left after the icon.
* Seen in a browser up to the 40px row height and the growing last child. The inner row on each cell compiles, but nobody has looked at it yet.

## Decisions

| Decision | Reason |
|---|---|
| Flex-wrap over grid | Grid auto-placement can't make one item span to the end of its row |
| Plain `@container` steps over a Kotlin loop | Seven lines are clearer than a generator |
| `--cols` from steps, not `round(down, 100cqi / 128px)` | Typed arithmetic isn't safe across browsers yet |
| Button widths in the stylesheet | A modifier is an inline style and can't reach an element's children. Wrapping each button was tried and reverted |
| The last child grows rather than an empty buttons cell | An empty cell wraps onto a blank line when the cells fill theirs exactly |
| No background on the container | It would fill the 2px gaps, which are part of the look |

## Tried and Dropped

* Buttons cell one column wide with `margin-left: auto`: it left a hole without background.
* 64px (later 48px) width per button with `JustifyContentEnd`: reverted to the `SpaceAround` row.
