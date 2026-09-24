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

Markup calls either with `invokeJs(args)`, as in `onClick = KoalaFun.toggleRootModifier.invokeJs(DayTheme)`. A script calls a `jsFunctionOf` function with `invoke` inside `jsScriptOf`.

## Root Settings

A site-wide display setting lives on `document.documentElement` and in `localStorage`, under the same key.

| Setting | Applied with | Initialized with |
|---|---|---|
| A class, on or off | `toggleRootModifier(mod)` | `initRootModifier(mod)` |
| An attribute with a value | `applyRootSwitch(name, value)` | `initRootSwitch(name, fallback)` |

An apply function writes the element and `localStorage` together. An init function runs in the head, before the body renders, so the stored setting is the first style shown. `initRootSwitch` sets the fallback when nothing is stored.

An app declares its root switches once, as a `HeadScriptConfig` of `RootSwitch(attribute, fallback)`, and builds it into `PageResource.headScript` with `buildHeadScript` at startup. `appHead` emits it first in the head of every page. The script defines both init functions, restores `DayTheme`, and invokes `initRootSwitch` for each switch. The app writes no JS for a setting.

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
