# Web Cleanup

Rehome the web module on the map `koala` now follows (`docs/packages/koala.md`, Packages), then map each package in its document.

## Map

| Package | Source set | Holds |
|---|---|---|
| `streetlight.web.component` | common | Components |
| `streetlight.web.layout` | common | Layouts, and forms that take no model |
| `streetlight.web.page` | common | Whole documents, the page chrome, and site-wide ids and attributes |
| `streetlight.web.shell` | common | The server-rendered content of a route |
| `streetlight.web.model` | both | Models the server renders from (common), view models and app wiring (js) |
| `streetlight.web.view` | both | The `Style` objects of views (common), views and the parts of views that take a model (js) |
| `streetlight.web.dom.component` | js | Components built in the browser |
| `streetlight.web.dom.layout` | js | Layouts built in the browser, and forms that take no model |
| `streetlight.web.dom.page` | js | The page frame in the browser: theme and footer |
| `streetlight.web.interop` | both | `wire` functions and calls from markup |
| `streetlight.web.io` | js | Clients |
| `streetlight.web.utility` | js | Utilities whose target is not a web type |

## Steps

1. 🍻 Survey the module.
2. 🍻 Settle the questions below.
3. 🍻 Bury the dead files.
4. 🍻 Move the files and rewrite packages and imports across `web`, `server` and the tests. Names a file saw through its old package are imported explicitly, keeping their precedence.
5. 🍻 Compile `web` and `server`, run the web tests.
6. 🍻 Write the package documents, with a map of each package, and `web.md` as the module document.
7. 🍻 Amend the documents that name the old packages, and the footer source paths (`sourcePathView`, `sourcePathShell`).

## Decisions

| Decision | Reason |
|---|---|
| Package names are singular | The project's pattern |
| A part of a view that takes a model goes to `view`; a form that takes none is a layout | Forms are layouts in this map |
| `editTalentForm` is kept | Talents return later |
| The web `ItemCache` is kept and koala's is buried | Its ideas are to be repurposed |
| A legacy shape still referenced moves by role | Forms changed approach recently; the stragglers convert in a later bounty |

## Dead Files Removed

`spotlightTab`, `eventParserDialog`, `eventsTab`, `galaxyEarthMenu`, `prototype`, `routeBlock`, `starBlock`, `earthChrome`, `layoutBuilderProto`, `viewStarEditorLegacy`, the empty `Form` object, koala's `ItemCache`.

## Questions

1. `viewTransitMap` is called nowhere, and it is the last user of the deprecated `Streetlight` interface. Bury both?

## Scratch Points

* 🍻 Buried: the five commented model files, the three light wires, `IntroStyle`, and `ViewModel`, which had no users.
* 🍻 `entityButtonsOf(entity)` is now `Entity.toButtons()`.
* 🍻 Renamed where the name was clear: `viewMedia` (was a `viewMediaConfig` in `viewMedia.kt`), `viewMediaConfig.kt` and `viewMediaConfigRoute` (were `viewPostEditor.kt` and `viewMediumUpdaterRoute`), `viewStarConfig.kt` (was `viewAccountUpdater.kt`), `viewMediaForgeRoute` (was `viewContentPosterRoute`), `viewLocationUpdaterRoute` (was `viewUpdateLocationRoute`), `configureEntityMarker.kt` (was `configureThumbMarker.kt`).
* Left as is: `viewEventUpdater` and `viewLocationUpdater`, since `viewLocationConfig` is a separate screen beside the location updater; the `Screen` entries `UpdateEvent` and `MediaUpdate`, since a screen's name sets its path; the `starBadge` inside `viewInbox`.
* `wireRouteDock` stays with its view in `viewRouteDock.kt`.
