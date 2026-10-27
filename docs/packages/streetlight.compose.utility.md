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
| `JankUtility.kt` | `LongFrameLog`, printing each frame that runs long, and `CompositionLog`, printing when a composable enters and leaves composition |

## Chasing Jank

Jank is measured before it is fixed. `LongFrameLog()` in a composable that lives the whole time, such as `AppPortal`, prints each frame over its threshold, and `CompositionLog(tag)` in a suspect, such as the map, prints when it comes and goes; read together, the times show which arrival stalls the frame. Both print under `[jank]`, are added for the hunt, and leave the call tree when it ends. A dev build stutters on its own now and then, so a single frame of a few dozen milliseconds is noise; a stall of hundreds is a find.
