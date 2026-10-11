# Package streetlight.web.utility

## Introduction

Utilities whose target is not a web type. A utility for a web type lives in the package of that type.

## Dependencies

| Package | Provides |
|---|---|
| `web` (Kotlin wrappers) | `Document`, events and `localStorage` |
| `kotlinx.serialization` | JSON |

## Utilities

| Utility | Role |
|---|---|
| [`localStoreOf`](../../web/src/jsMain/kotlin/streetlight/web/utility/localStoreOf.kt) | A store kept in `localStorage` |
| [`setTitle`, `requireElement`](../../web/src/jsMain/kotlin/streetlight/web/utility/DocumentUtility.kt) | The tab title, and elements by id |
| [`Event.element`](../../web/src/jsMain/kotlin/streetlight/web/utility/EventFunctons.kt) | The element an event targets |
