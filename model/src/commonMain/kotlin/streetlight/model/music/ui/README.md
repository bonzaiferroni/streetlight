# Package streetlight.model.music.ui

## Introduction

The presentation model of notation: the domain model of `streetlight.model.music` translated into a structure a UI can consume, on web or compose. Each toolkit renders from it and supplies no layout of its own.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.music` | The songs and tracks it lays out as notation |

## Layout

A type here states what is drawn and where, as shares of a section and staff steps. It holds no toolkit type and no unit of a screen, such as a pixel or a color.

A note's horizontal place is its onset, so it meets a playback cursor as it sounds.

Measures are numbered from 1 across a notation, and a pickup is measure 0. A pickup's width is in addition to the section it leads. Each notation of an arrangement starts its measures at its own start and breaks them into lines by its own `measuresPerLine`, and the pickup joins the first line.

A `StaffWindow` keeps room below its staff for lines of lyrics, each as tall as the notation of a lyric track, and every step of a notation counts from the bottom of that room. A notation drawn once carries each different line sung to it, and one drawn for a single time through carries the line of that time.
