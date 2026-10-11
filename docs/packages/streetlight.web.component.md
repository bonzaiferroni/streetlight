# Package streetlight.web.component

## Introduction

The app's components declared on `FlowContent`, which the server renders, and the `Style` objects of the app's components in both halves.

## Dependencies

| Package | Provides |
|---|---|
| `koala.component`, `koala.layout` | koala's components and layouts |
| `koala.modifier` | Modifiers and classes |
| `streetlight.web.page` | `PopoverId` and `AppAttribute` |
| `streetlight.model` | Entities and routes |

## Menu Buttons

A button that opens a popover menu targets the menu's `PopoverId` and sets the menu's data on itself as attributes. Its menu is wired in `streetlight.web.interop`, as specified there under Popover Menus.

## Components

### Buttons and Menus

| Component | Role |
|---|---|
| [`starToggle`](../../web/src/commonMain/kotlin/streetlight/web/component/StarToggle.kt) | Lights or unlights an item for the star, with its count |
| [`curatorBadge`, `polarBadge`](../../web/src/commonMain/kotlin/streetlight/web/component/CuratorMenu.kt) | A post's curator marks, opening the curator menu |
| [`postMenu`](../../web/src/commonMain/kotlin/streetlight/web/component/postMenu.kt) | Opens the menu of a post |
| [`tagFilterMenu`](../../web/src/commonMain/kotlin/streetlight/web/component/tagFilterMenu.kt) | Opens the menu that filters a feed by tag |
| [`createPostMenu`](../../web/src/commonMain/kotlin/streetlight/web/component/createPostMenu.kt) | Opens the create post menu |
| [`galaxyMenu`](../../web/src/commonMain/kotlin/streetlight/web/component/galaxyMenu.kt) | Opens the menu of the star's galaxies |
| [`buttonOf`](../../web/src/commonMain/kotlin/streetlight/web/component/buttonOf.kt) | A button linking to a galaxy |

### Logos and Badges

| Component | Role |
|---|---|
| [`textLogo`, `topLogo`](../../web/src/commonMain/kotlin/streetlight/web/component/textLogo.kt) | The Streetlight wordmark with a glowing flame |
| [`iconLogo`](../../web/src/commonMain/kotlin/streetlight/web/component/iconLogo.kt) | The flame logo as a glowing icon |
| [`logoProto`](../../web/src/commonMain/kotlin/streetlight/web/component/logoProto.kt) | The large flame logo, glowing |
| [`starBadge`](../../web/src/commonMain/kotlin/streetlight/web/component/starBadge.kt) | The signed-in star's image, filled in by the client |

### Feedback

| Component | Role |
|---|---|
| [`workSignal`](../../web/src/commonMain/kotlin/streetlight/web/component/workSignal.kt) | A spinning icon that shows work in progress |
