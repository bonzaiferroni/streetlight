# koala.html

## Introduction

Server-renderable components declared on `FlowContent`.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | Modifiers and classes |
| `koala.interop` | `KoalaFun` for markup that calls the browser |
| `kampfire.model` | Shared model types |

## Images

An image component takes the `Image`, not a URL of one size, so it renders the image's source set. A new image component configures its `img` with `configureImage`.

A caller changes the fit of a `containImage` on its root, which the content image inherits.

## Root Switch

`rootSwitch(attribute, mod) { value -> }` renders a row of element buttons, one per value of an enum root setting. The lambda builds each button's content. A button applies its value with `applyRootSwitch` and carries it under `attribute.optionAttribute`.

The app marks the selected button by adding `rootSwitchCss(attribute, values)` to its own stylesheet. The default style sets `color` to `--primary-fg`; a caller passes another style to change it.

The setting is restored on load through the app's `HeadScriptConfig`, described in `koala.interop.md`.
