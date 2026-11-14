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
| [`IdbDatabase`, `IdbStore`](IdbDatabase.kt) | A browser IndexedDB database, and an object store of it holding values as JSON |
| [`localStoreOf`](localStoreOf.kt) | A store kept in `localStorage` |
| [`setTitle`, `requireElement`](DocumentUtility.kt) | The tab title, and elements by id |
| [`downloadFile`](downloadFile.kt) | Hands the viewer bytes to save as a file |
| [`Event.element`](EventFunctons.kt) | The element an event targets |
