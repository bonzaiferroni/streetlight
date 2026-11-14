# Package koala.utility

## Introduction

Utilities whose target is not a koala type, such as coroutines, flows and JSON. A utility for a koala type lives in the package of that type.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.coroutines` | `Flow` and `CoroutineScope`, the targets of most of its utilities |
| `kotlinx.serialization` | JSON |

## Utilities

| Utility | Role |
|---|---|
| [`launch`](CoroutineUtility.kt) | Launches a named coroutine, as [`koala`](../../../../../README.md) specifies |
| [`LaunchTelemetry`](LaunchTelemetry.kt) | The name of a launched coroutine, carried in its context |
| [`DropWhileBusy`](DropWhileBusy.kt) | Launches a block only when its previous launch has finished |
| [`FlowUtility`](FlowUtility.kt) | Flows mapped and emitting only on change |
| [`jsonConfig`, `sanitizeJson`](Json.kt) | The app's JSON format, and JSON made safe for a page |
| [`getTimeZoneId`](../../../../jsMain/kotlin/koala/utility/TimeZoneUtility.kt) | The browser's time zone id |
| [`jsObject`](../../../../jsMain/kotlin/koala/utility/utils.kt) | A JS object built by a block, deprecated for external interfaces |
