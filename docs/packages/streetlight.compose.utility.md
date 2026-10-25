# Package streetlight.compose.utility

## Introduction

Utilities whose target is not a compose type.

## Dependencies

| Package | Provides |
|---|---|
| `kampfire.model` | `Tap` |
| `koala.modifier` | `Rgb` and `Glow` |

## Utilities

| File | Holds |
|---|---|
| `TapUtility.kt` | `Tap.collectAsState()`, a lens as Compose state from its `now` |
| `ColorUtility.kt` | Colors of koala's types, and the mixes and turns the web's CSS makes, such as `mixSrgb` for `color-mix(in srgb, …)` |
