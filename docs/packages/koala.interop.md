# koala.interop

Calls between server-rendered markup and the browser. Markup names a global JS function; the browser defines it.

## Global Functions

| Kind | Declared as | Defined by |
|---|---|---|
| Kotlin function exposed to markup | `JsSignature` in `KoalaFun` | A `KtFunction` in `interopUtilities` (jsMain), added to `globalThis` at startup |
| Plain JS function for the head | `jsFunctionOf` in `KoalaFun` | `define` in `KoalaHeadScript` |

Markup calls either with `invokeJs(args)`, as in `onClick = KoalaFun.toggleRootModifier.invokeJs(DayTheme)`. A script calls a `jsFunctionOf` function with `invoke` inside `jsScriptOf`.

## Root Settings

A site-wide display setting lives on `document.documentElement` and in `localStorage`, under the same key.

| Setting | Applied with | Initialized with |
|---|---|---|
| A class, on or off | `toggleRootModifier(mod)` | `initRootModifier(mod)` |
| An attribute with a value | `applyRootSwitch(name, value)` | `initRootSwitch(name, fallback)` |

An apply function writes the element and `localStorage` together. An init function runs in the head, before the body renders, so the stored setting is the first style shown. `initRootSwitch` sets the fallback when nothing is stored.

`KoalaHeadScript` defines both init functions and invokes `initRootModifier` for `DayTheme`. `appHead` emits it before the app's head content, so an app script in the head can invoke them for its own settings.
