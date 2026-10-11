# Side Tabs

A new `sideTabs` in `koala.dom.layout`: a column of tabs bound to a `MutableTap<String>`, the selected one on a lighter background, beside a panel that `content` builds for it in a `flowBlock`. An overload binds the entries of a `Labeled` enum and hands `content` the entry. `tagMenu` shows the tags of each `EventCategory` with it.

## Shape

* `SideTabsStyle` and `SideTabsCss` in `koala.layout/sideTabs.kt`, registered with the other stylesheets in `ServerResource`.
* `sideTabs(options, state, mod, content)` and `sideTabs<E>(state, mod, content)` in `koala.dom.layout/sideTabs.kt`: a `tablist` of `tab` buttons marked by `aria-selected`, and a `flowBlock` panel.
* Each tab carries `TextSmall` and `TextUppercase` as modifiers. The stylesheet sets the padding and the selected background.
* The rail and panel stretch to the height of the root, which a caller limits, and each scrolls on its own. Tabs sit on `--card-bg`, 2px apart.

## Steps

1. 🍻 Write `optionRail` in `koala.dom.component`.
2. 🍻 Fold the panel in and move it to `koala.dom.layout` as `sideTabs`.
3. 🍻 Register `SideTabsCss`.
4. 🍻 Show the categories in `tagMenu` with `sideTabs`.
5. 🍻 Compile `koala`, `web` and `server`.
6. 🍻 List `sideTabs` in `koala.dom.layout.md`.

## Decisions

| Decision | Reason |
|---|---|
| The selected option is marked by a background, not an outline | The rail sits in a container with a border radius |
| The layout holds the panel | Every consumer wants the same column-and-panel shape |
| The selected background is `rgba(var(--ink), .1)` on both themes | A start, calibrated in use |
| The rail does not clip its corners | The consumer controls the container's shape |

## Questions
