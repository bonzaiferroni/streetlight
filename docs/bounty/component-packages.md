# Component Packages

Move UI definitions that represent components out of the layout packages, so each package holds one level of the hierarchy.

## Hierarchy

| Level | Definition |
|---|---|
| Component | A single element of UI, even when built from other components |
| Layout | A composed section of UI, calling components only to lay them out |
| View | A layout associated with a model that holds UI state |

## Steps

1. koala: move the components in `koala.layout` to `koala.component`, and those in `koala.dom.layout` to `koala.dom.component`. Fix imports.
2. Compile koala and its dependents.
3. Docs: record the hierarchy, and move the moved entries between package documents.
4. web: classify `streetlight.web.layout` and `streetlight.web.dom.layout` (crossroads).

## Decisions

- Moved from `koala.layout`: `div`, `box`, `column`, `row`, `section`, `card` (with `cardOf`), `spacer` (with `centeredHeading`), `grid`, `carousel`, `listing` (`olist`, `ulist`), `listItem`.
- Moved from `koala.dom.layout`: `div`, `box`, `column`, `row`, `section`, `card`, `grid`.
- `popover` and `dialog`, both halves, moved to the component packages.
- `messageBox`, both halves, moved to the layout packages. `fileDrop` and `imageDrop` stay components on `flowBlock`.
- `taskContent` and `linksFormSection` moved to `streetlight.web.view`. Web layouts that take a model without UI state, such as `cardOf`, stay layouts. Form parts, `entityParts` and `smallGalaxyCard` stay layouts.

## Progress

- [x] Step 1
- [x] Step 2: koala, web and server compile
- [x] Step 3: hierarchy recorded in `koala.md`
- [x] Step 4

## Open Questions

None.
