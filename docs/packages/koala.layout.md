# Package koala.layout

## Introduction

Layouts declared on `FlowContent`, which compose components in markup built without the bundle, and the stylesheets and `Style` objects of every layout, including those whose function lives in `koala.dom.layout`.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | Modifiers and classes |
| `koala.component` | The components layouts are built from |
| `koala.page` | `Id` |
| `koala.interop` | `KoalaFun` for markup that calls the browser |

## Layouts

### Panels

| Layout | Role |
|---|---|
| [`tabs`](../../koala/src/commonMain/kotlin/koala/layout/tabs.kt) | Tabs over panels, wired by the browser with `initTabs` |
| [`TabScope`](../../koala/src/commonMain/kotlin/koala/layout/TabScope.kt) | Collects the tabs of a `tabs` block |

### Stylesheets of Browser Layouts

| Stylesheet | Layout in `koala.dom.layout` |
|---|---|
| [`MessageBox`](../../koala/src/commonMain/kotlin/koala/layout/messageBox.kt) | `messageBox` |
| [`FlowBlockStyle`](../../koala/src/commonMain/kotlin/koala/layout/flowBlock.kt) | `flowBlock` |
| [`ItemsBlockStyle`](../../koala/src/commonMain/kotlin/koala/layout/itemsBlock.kt) | `itemsBlock` |
| [`LazyColumnStyle`](../../koala/src/commonMain/kotlin/koala/layout/LazyColumnStyle.kt) | `lazyColumn` |
