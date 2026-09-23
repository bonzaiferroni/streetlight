# Entity Cell

A cell shows one property of an entity inside a `cellBlock`. Three functions build a cell today: `cell`, `cellContent` and `linkCell`. Each takes a `Modifier`, and the minimum widths differ: `cell` uses `MinWidth(16)` and a linked `linkCell` uses `MinWidth(12)`. The `cellContentOf` overloads return `FlowContent.() -> Unit` blocks that mix property cells with a `buttonsCell`. `FeedEntity.getCells` resolves those blocks for each entity type.

`EntityCell` (icon, text, optional `Url`) is declared in `CellContent.kt`. Every entity cell is styled the same way.

Bounty:
* One function in `CellContent.kt` renders an `EntityCell`, linked when `url` is set, and replaces `cell`, `cellContent` and `linkCell`
* The single-property helpers (`startsAtCell`, `dateCell`, `costCell`, `starCell`, `postedAtCell`, `linkCell(ExtraLink)`) return an `EntityCell`
* The `cellContentOf` overloads split in two: one produces a `List<EntityCell>`, and `buttonCellsOf` keeps the `buttonsCell` blocks
* `FeedEntity.getCells` returns `List<EntityCell>?`, and a separate property resolves the button cells
* Components that take a cell block take the entity cells and the button cells separately: `feedRow`/`configureFeedRow` (both source sets), `featureHeader`, `largePostCard`, `focusPanel`
* Convert the call sites that build cells inline: `eventShell`, `largeEventPostCard`, `headerOf(media)`, `smallGalaxyCard`, `postRow(EventEdit)`
* Decide what happens to `textPropertyCell` (has no icon, never called), `exampleStartsAtCell` (empty body) and `FeedEntity.cellContent` (never called)
* Docs: the Cells section of `streetlight.web.layouts.md`, and the file it names for `FeedEntity` properties (`PostProperty.kt`, which is now `FeedEntityProperty.kt`)
