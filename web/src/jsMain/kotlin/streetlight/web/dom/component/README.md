# Package streetlight.web.dom.component

## Introduction

The app's components built in the browser on `AppendScope` or `ViewScope`.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom`, `koala.dom.component`, `koala.dom.layout` | The view runtime, and koala's components and layouts |
| `streetlight.web.component` | The stylesheets and server-rendered variants of the components here |
| `kampfire.model` | `MutableTap` |

## Components

| Component | Role |
|---|---|
| [`slider`](slider.kt) | A range input bound to a field, with an optional icon |
| [`popoverMenu`](PopoverMenu.kt) | A popover shared by every button that targets its id |
| [`configHeading`](configHeading.kt) | The heading of a `mainBody`, linking to what the view configures |
| [`workSignal`](workSignal.kt) | A spinning icon shown while work is in progress |
| [`comingSoon`](comingSoon.kt) | An animation for a feature not yet built |
| [`configureEntityMarker`](configureEntityMarker.kt) | The body of an entity's map marker |
| [`configureEventMarkerBody`](configureEventMarkerBody.kt) | The body of an event's map marker |
