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

The module follows the map in `koala.md` under Packages.

| Package | Holds |
|---|---|
| `streetlight.web.component` | The app's components |
| `streetlight.web.layout` | The app's layouts, and forms that take no model |
| `streetlight.web.page` | Whole documents, the page chrome, and site-wide ids and attributes |
| `streetlight.web.shell` | The server-rendered content of a route |
| `streetlight.web.model` | View models and services, and the models the server renders from |
| `streetlight.web.view` | Views and the parts of views that take a model, and the stylesheets of views |
| `streetlight.web.interop` | Global functions markup calls, and the `wire` functions |
| `streetlight.web.io` | The browser's transport to the server, services built on its streams, and clients of outside services |
| `streetlight.web.subApp` | The views, services and fetchers of sub-apps |
| `streetlight.web.utility` | Utilities whose target is not a web type |

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
