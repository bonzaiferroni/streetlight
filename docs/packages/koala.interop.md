# Package koala.interop

## Introduction

Calls between server-rendered markup and the browser. Markup names a global JS function; the browser defines it.

## Dependencies

| Package | Provides |
|---|---|
| `koala.modifier` | `jsScriptOf`, `Attribute` and root modifier classes |
| `koala.page` | `Id` arguments |
| `web` (Kotlin wrappers) | DOM and `localStorage` access in `jsMain` |

## Global Functions

| Kind | Declared as | Defined by |
|---|---|---|
| Kotlin function exposed to markup | `JsSignature` in `KoalaFun` | A `KtFunction` in `interopUtilities` (jsMain), added to `globalThis` at startup |
| Plain JS function for the head | `jsFunctionOf` in `KoalaFun` | `define` in `buildHeadScript` |

Markup calls either with `invokeJs(args)`, as in `onClick = KoalaFun.toggleRootModifier.invokeJs(DayTheme)`. `ThisElement` passes the element holding the handler and `ThisEvent` the event it handles. A script calls a `jsFunctionOf` function with `invoke` inside `jsScriptOf`.

## Root Settings

A site-wide display setting lives on `document.documentElement` and in `localStorage`, under the same key.

| Setting | Applied with | Initialized with |
|---|---|---|
| A class, on or off | `toggleRootModifier(mod)` or `setRootModifier(mod, isOn)` | `initRootModifier(mod)` |
| An attribute with a value | `applyRootSwitch(name, value)` | `initRootSwitch(name, fallback)` |
| The root's inline style | `applyRootStyle(key, cssText)` | `initRootStyle(key)` |

An apply function writes the element and `localStorage` together. An init function runs in the head, before the body renders, so the stored setting is the first style shown. `initRootSwitch` sets the fallback when nothing is stored.

An app declares its root settings once, as a `HeadScriptConfig` of `RootSwitch(attribute, fallback)` entries, root modifier classes and an optional root style key, and builds it into `PageResource.headScript` with `buildHeadScript` at startup. `appHead` emits it first in the head of every page. The script defines the init functions, restores `DayTheme`, and invokes the matching init function for each setting. The app writes no JS for a setting.

The root style is a single key, since its text replaces the root's whole inline style.

A root switch has these parts:

| Part | Declared in |
|---|---|
| The `Attribute` | The app |
| `RootSwitch` in the app's `HeadScriptConfig` | The app |
| `rootSwitch`, the control | The app's markup, from `koala.component` |
| `rootSwitchCss`, marking the selected option | The app's stylesheet, listed in `KtStyles` |
| CSS that reads the attribute | The app's stylesheet |
| `applyRootSwitch` | `interopUtilities` |
| `initRootSwitch` | `buildHeadScript` |

## Wiring

A `wire` function binds a view to markup the server rendered without the bundle. The markup side, such as `wiringBlock`, lives here beside the `wire` function that finds it.

## Interop

| Part | Role |
|---|---|
| [`KoalaFun`](../../koala/src/commonMain/kotlin/koala/interop/KoalaFun.kt) | The global functions Koala defines for markup |
| [`GlobalFunction`, `ThisElement`, `ThisEvent`](../../koala/src/commonMain/kotlin/koala/interop/GlobalFunction.kt) | A JS function markup calls by name, and its element and event arguments |
| [`JsSignature`, `jsFunctionOf`](../../koala/src/commonMain/kotlin/koala/interop/JsSignature.kt) | A function declared by name, or written in JS |
| [`JsAction`](../../koala/src/commonMain/kotlin/koala/interop/JsAction.kt) | A statement of JS to run inline |
| [`invoke`](../../koala/src/commonMain/kotlin/koala/interop/invoke.kt) | The JS call of a function with string arguments |
| [`buildHeadScript`, `HeadScriptConfig`](../../koala/src/commonMain/kotlin/koala/interop/buildHeadScript.kt) | The head script that restores root settings |
| [`KtFunction`](../../koala/src/jsMain/kotlin/koala/interop/KtFunction.kt) | A Kotlin function exposed to markup |
| [`interopUtilities`](../../koala/src/jsMain/kotlin/koala/interop/interopUtilities.kt) | The Kotlin functions Koala exposes, and the root setting functions |
| [`initTabs`](../../koala/src/jsMain/kotlin/koala/interop/initTabs.kt) | Wires server-rendered tabs |
| [`wiringBlock`](../../koala/src/commonMain/kotlin/koala/interop/wireBlock.kt) | An element the browser wires a view into |
| [`wireBlock`](../../koala/src/jsMain/kotlin/koala/interop/wireBlock.kt) | Mounts a child view into a server-rendered element |
| [`wireGeoMap`, `wireMapContext`](../../koala/src/jsMain/kotlin/koala/interop/wireGeoMap.kt) | Attaches the shared map to its mount |
