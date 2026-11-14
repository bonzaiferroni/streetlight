# Package koala.layout

## Introduction

Layouts declared on `FlowContent`, which compose components in markup built without the bundle, and the stylesheets and `Style` objects of every layout, including those whose function lives in `koala.dom.layout`.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | `modify`, and the classes and styles it applies to elements |
| `koala.component` | The components layouts are built from |
| `koala.page` | `Id` |
| `koala.interop` | `KoalaFun` for markup that calls the browser |

## Layouts

### Panels

| Layout | Role |
|---|---|
| [`tabs`](tabs.kt) | Tabs over panels, wired by the browser with `initTabs` |
| [`TabScope`](TabScope.kt) | Collects the tabs of a `tabs` or `transTabs` block |
| [`transTabs`](transTabs.kt) | Tabs whose changes run as view transitions, wired by the browser with `initTransTabs` |

### Stylesheets of Browser Layouts

| Stylesheet | Layout in `koala.dom.layout` |
|---|---|
| [`MessageBox`](messageBox.kt) | `messageBox` |
| [`FlowBlockStyle`](flowBlock.kt) | `flowBlock` |
| [`ItemsBlockStyle`](itemsBlock.kt) | `itemsBlock` |
| [`LazyColumnStyle`](LazyColumnStyle.kt) | `lazyColumn` |
