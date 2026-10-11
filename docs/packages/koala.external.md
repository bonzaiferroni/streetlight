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
| [`BrowserApi`](../../koala/src/jsMain/kotlin/koala/external/BrowserApi.kt) | Browser options missing from the wrappers |
| [`IntersectionObserver`](../../koala/src/jsMain/kotlin/koala/external/IntersectionObserver.kt) | `IntersectionObserver`, and `onView` handlers on an element |
| [`ResizeObserver`](../../koala/src/jsMain/kotlin/koala/external/ResizeObserver.kt) | `ResizeObserver`, and an element's size as a flow |
| [`MaplibreApi`](../../koala/src/jsMain/kotlin/koala/external/MaplibreApi.kt) | MapLibre GL |
| [`EChartsApi`](../../koala/src/jsMain/kotlin/koala/external/EChartsApi.kt) | ECharts |
| [`LottieApi`](../../koala/src/jsMain/kotlin/koala/external/LottieApi.kt) | Lottie |
| [`SimpleMDEApi`](../../koala/src/jsMain/kotlin/koala/external/SimpleMDEApi.kt) | SimpleMDE |
| [`RealtimeGtfsApi`](../../koala/src/jsMain/kotlin/koala/external/RealtimeGtfsApi.kt) | The GTFS Realtime feed |
