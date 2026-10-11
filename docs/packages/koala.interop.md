# Package koala.interop

## Introduction

Calls between server-rendered markup and the browser. Markup names a global JS function; the browser defines it.

## Dependencies

| Package | Provides |
|---|---|
| `koala.modifier` | `jsScriptOf`, `Attribute` and root modifier classes |
| `koala.html` | `Id` and `Class` arguments |
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
| `rootSwitch`, the control | The app's markup, from `koala.html` |
| `rootSwitchCss`, marking the selected option | The app's stylesheet, listed in `KtStyles` |
| CSS that reads the attribute | The app's stylesheet |
| `applyRootSwitch` | `interopUtilities` |
| `initRootSwitch` | `buildHeadScript` |
