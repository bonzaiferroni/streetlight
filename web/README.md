# Module web

## Introduction

The Streetlight web client: the pages the server renders, and the views the browser builds over them.

## Dependencies

| Package | Provides |
|---|---|
| `koala` | Components, layouts, the page frame and the view runtime |
| `model` | Entities, DTOs, routes and `Api` |
| `kampfire` | Wire and state types |

## Packages

The module follows the map in [`koala`](../koala/README.md) under Packages.

| Package | Holds |
|---|---|
| [`streetlight.web.component`](src/commonMain/kotlin/streetlight/web/component/README.md) | The app's components |
| [`streetlight.web.layout`](src/commonMain/kotlin/streetlight/web/layout/README.md) | The app's layouts, and forms that take no model |
| [`streetlight.web.page`](src/commonMain/kotlin/streetlight/web/page/README.md) | Whole documents, the page chrome, and site-wide ids and attributes |
| [`streetlight.web.shell`](src/commonMain/kotlin/streetlight/web/shell/README.md) | The server-rendered content of a route |
| [`streetlight.web.model`](src/commonMain/kotlin/streetlight/web/model/README.md) | View models and services, and the models the server renders from |
| [`streetlight.web.view`](src/commonMain/kotlin/streetlight/web/view/README.md) | Views and the parts of views that take a model, and the stylesheets of views |
| [`streetlight.web.interop`](src/commonMain/kotlin/streetlight/web/interop/README.md) | Global functions markup calls, and the `wire` functions |
| [`streetlight.web.io`](src/jsMain/kotlin/streetlight/web/io/README.md) | The browser's transport to the server, services built on its streams, and clients of outside services |
| [`streetlight.web.idb`](src/jsMain/kotlin/streetlight/web/idb/README.md) | A relational layer over the browser's IndexedDB |
| [`streetlight.web.subApp`](src/jsMain/kotlin/streetlight/web/subApp/README.md) | The views, services and fetchers of sub-apps |
| [`streetlight.web.utility`](src/jsMain/kotlin/streetlight/web/utility/README.md) | Utilities whose target is not a web type |
| [`streetlight.web.dom.component`](src/jsMain/kotlin/streetlight/web/dom/component/README.md) | The app's components that need the browser, such as `popoverMenu` and `slider` |
| [`streetlight.web.dom.layout`](src/jsMain/kotlin/streetlight/web/dom/layout/README.md) | The app's layouts that need the browser, including the forms that take no model |
| [`streetlight.web.dom.page`](src/jsMain/kotlin/streetlight/web/dom/page/README.md) | The page theme and a view's footer, set in the browser |
| [`streetlight.web.integration`](src/jsTest/kotlin/streetlight/web/integration/README.md) | Browser tests of views and their view models |
| [`streetlight.web.promo`](src/jsPromo/kotlin/streetlight/web/promo/README.md) | The shots of promo videos: real views mounted over the test fakes |

The browser half of the UI lives under `streetlight.web.dom`, in `streetlight.web.dom.component`, `streetlight.web.dom.layout` and `streetlight.web.dom.page`, mirroring the server-rendered packages as `koala.dom` does.

A part of a view that takes a model holding UI state belongs to `streetlight.web.view`. A layout that takes a model without UI state, and a form that takes none, stay layouts.

## Sub-Apps

A sub-app is a web app of its own built from the module's parts, such as the LocationApp a location serves at its subdomain. It is named `FooApp`.

| Part | Where |
|---|---|
| Entry | A standalone compilation named `fooApp` in `standalones.txt`, whose `main()` in `src/jsStandalone/fooApp` only calls `viewFooApp()` |
| Views, Koin module, fetcher | `streetlight.web.subApp` |
| Page | `fooAppPage` in `streetlight.web.page` |
| Shells | `fooAppShell` and the like in `streetlight.web.shell` |
| Screens and routes | `FooAppScreen` in `streetlight.model.ui` |
| Bundle | `JsBundle.fooApp` |

A sub-app shares no runtime with `web.js`. Nothing in `jsMain` outside `streetlight.web.subApp` references it.
