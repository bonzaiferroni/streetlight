# Package streetlight.model.music.ui

## Introduction

The presentation model of notation: the domain model of `streetlight.model.music` translated into a structure a UI can consume, on web or compose. Each toolkit renders from it and supplies no layout of its own.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.music` | The domain model |

## Layout

A type here states what is drawn and where, as shares of a section and staff steps. It holds no toolkit type and no unit of a screen, such as a pixel or a color.

A note's horizontal place is its onset, so it meets a playback cursor as it sounds.
