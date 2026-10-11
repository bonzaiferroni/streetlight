# Package koala.utility

## Introduction

Utilities whose target is not a koala type, such as coroutines, flows and JSON. A utility for a koala type lives in the package of that type.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.coroutines` | Coroutines and flows |
| `kotlinx.serialization` | JSON |

## Utilities

| Utility | Role |
|---|---|
| [`launch`](../../koala/src/commonMain/kotlin/koala/utility/CoroutineUtility.kt) | Launches a named coroutine, as `koala.md` specifies |
| [`LaunchTelemetry`](../../koala/src/commonMain/kotlin/koala/utility/LaunchTelemetry.kt) | The name of a launched coroutine, carried in its context |
| [`DropWhileBusy`](../../koala/src/commonMain/kotlin/koala/utility/DropWhileBusy.kt) | Launches a block only when its previous launch has finished |
| [`FlowUtility`](../../koala/src/commonMain/kotlin/koala/utility/FlowUtility.kt) | Flows mapped and emitting only on change |
| [`jsonConfig`, `sanitizeJson`](../../koala/src/commonMain/kotlin/koala/utility/Json.kt) | The app's JSON format, and JSON made safe for a page |
| [`getTimeZoneId`](../../koala/src/jsMain/kotlin/koala/utility/TimeZoneUtility.kt) | The browser's time zone id |
| [`jsObject`](../../koala/src/jsMain/kotlin/koala/utility/utils.kt) | A JS object built by a block, deprecated for external interfaces |
