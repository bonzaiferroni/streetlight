# Package koala.external

## Introduction

`external` declarations of the browser and library APIs that the Kotlin wrappers do not cover. A declaration mirrors the API of its library.

## Dependencies

| Package | Provides |
|---|---|
| `web` (Kotlin wrappers) | DOM types the declarations take and return |

## Declarations

| File | Declares |
|---|---|
| [`BrowserApi`](BrowserApi.kt) | Browser options missing from the wrappers |
| [`IntersectionObserver`](IntersectionObserver.kt) | `IntersectionObserver`, and `onView` handlers on an element |
| [`ResizeObserver`](ResizeObserver.kt) | `ResizeObserver`, and an element's size as a flow |
| [`MaplibreApi`](MaplibreApi.kt) | MapLibre GL |
| [`EChartsApi`](EChartsApi.kt) | ECharts |
| [`LottieApi`](LottieApi.kt) | Lottie |
| [`SimpleMDEApi`](SimpleMDEApi.kt) | SimpleMDE |
| [`RealtimeGtfsApi`](RealtimeGtfsApi.kt) | The GTFS Realtime feed |
