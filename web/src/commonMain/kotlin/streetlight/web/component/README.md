# Package streetlight.web.component

## Introduction

The app's components declared on `FlowContent`, which the server renders, and the `Style` objects of the app's components in both halves.

## Dependencies

| Package | Provides |
|---|---|
| `koala.component`, `koala.layout` | The building blocks and arrangements of markup the app's components extend |
| `koala.modifier` | `modify`, and the classes and styles it applies to elements |
| `streetlight.web.page` | `PopoverId` and `AppAttribute` |
| `streetlight.model` | Entities and routes |

## Menu Buttons

A button that opens a popover menu targets the menu's `PopoverId` and sets the menu's data on itself as attributes. Its menu is wired in `streetlight.web.interop`, as specified there under Popover Menus.

## Components

### Buttons and Menus

| Component | Role |
|---|---|
| [`starToggle`](StarToggle.kt) | Lights or unlights an item for the star, with its count |
| [`curatorBadge`, `polarBadge`](CuratorMenu.kt) | A post's curator marks, opening the curator menu |
| [`postMenu`](postMenu.kt) | Opens the menu of a post |
| [`tagFilterMenu`](tagFilterMenu.kt) | Opens the menu that filters a feed by tag |
| [`createPostMenu`](createPostMenu.kt) | Opens the create post menu |
| [`galaxyMenu`](galaxyMenu.kt) | Opens the menu of the star's galaxies |
| [`buttonOf`](buttonOf.kt) | A button linking to a galaxy |
| [`connectPicker`](connectPicker.kt) | The four connect types as links, the chosen one lit |

### Logos and Badges

| Component | Role |
|---|---|
| [`textLogo`, `topLogo`](textLogo.kt) | The Streetlight wordmark with a glowing flame |
| [`iconLogo`](iconLogo.kt) | The flame logo as a glowing icon |
| [`logoProto`](logoProto.kt) | The large flame logo, glowing |
| [`starBadge`](starBadge.kt) | The signed-in star's image, filled in by the client |

### Feedback

| Component | Role |
|---|---|
| [`workSignal`](workSignal.kt) | A spinning icon that shows work in progress |
