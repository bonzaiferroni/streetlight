# Koala Cleanup

Rehome everything in `koala.html` (commonMain) and `koala.dom` (jsMain) into packages by role, then map each package in its document. The web module follows in a later bounty, on the same map.

## Map

The map as built is recorded in `docs/packages/koala.md` under Packages. The dom-aware UI lives under `koala.dom`, in `koala.dom.component`, `koala.dom.layout`, `koala.dom.page` and `koala.dom.markdown`, mirroring the dom-agnostic packages.

## Rules

* A `Utility` file lives in the package of its target type, so its extensions need no extra import.
* A `Style` object, and its `Css` stylesheet, live with the component that references them.
* A model declared in jsMain that does not need the browser moves to commonMain.
* Commented code is raised as a question before it is removed.

## Steps

1. 🍻 Classify every file in `koala.html` and `koala.dom`.
2. 🍻 Remove the dead files.
3. 🍻 Move the files and rewrite their packages and imports, across every module.
4. 🍻 Split the dom-aware half into `koala.dom.*` packages.
5. 🍻 Move `MenuAction`, `MessageStore`, `ChartSeries` and `ItemCache` to commonMain.
6. 🍻 Compile `koala`, `model`, `web` and `server`, and run the koala and web tests.
7. 🍻 Write the package documents, with a map of each package's contents.
8. 🍻 Amend the documents that name the old packages.

## Decisions

| Decision | Reason |
|---|---|
| `koala.dom` stays, narrowed to the view runtime | The runtime is neither component, layout nor view |
| No `koala.view` | Views belong to the web module |
| `MarkerStyle` and `configureMarker` stay in `koala.model` | `GeoMarker` builds its own markup; see Scratch Points |
| `CaretUtility` goes with `koala.markdown` | Only the markdown editor uses it |
| `invoke` goes to `koala.interop` | It writes a JS call |
| `TableCss` goes to `koala.markdown` | It styles prose tables |
| The dom-aware half takes `koala.dom.*` packages | A view star-imports them and sees only the `AppendScope` and `ViewScope` variants |
| Inside koala, imports of moved names are explicit | Keeps the precedence the old shared package gave koala names over `kotlinx.html` |
| `koala.interop`, `koala.model`, `koala.utility` and `koala.external` keep both halves in one package | They hold no overloads that differ only by receiver |

## Dead Files Removed

`ElementScope`, `RenderJob`, `requests`, `routeBlock`, dom `imageChooser`, html `ElementEvent`, `stylesheets`, `ImageChooserCss`, `swap`, `initKoalaDom`, `SelectionApi`, `sidebar`.

## Scratch Points

* `GeoMarker` builds its own markup inside `koala.model`, with `MarkerStyle` and `configureMarker` beside it. Kept as is for now.
* 🍻 `textLogo`, `topLogo`, `iconLogo` and `logoProto` moved to `streetlight.web.pages`, beside every page that draws them.
