# Package streetlight.model.music.ui

## Introduction

The presentation model of notation: the domain model of `streetlight.model.music` translated into a structure a UI can consume, on web or compose. Each toolkit renders from it and supplies no layout of its own.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.music` | The songs and tracks it lays out as notation |

## Music Source

A `MusicSource` unfolds a target, the song or one notation, into a measure grid and a `TrackSource` for each track it holds. Tick 0 is the target's first downbeat, measure 0 is the pickup before it, and every other measure is a full measure of the time signature, so a measure's tick and length follow from its index.

A track's events are sorted by tick, each carrying the `notationId` and index of the note it comes from, so an edit traces back to its notation. A melody track carries the syllables of its lyric track among its events.

A track's measures and lines are spans of indices: a measure over the track's events, and a line over the measures, broken by each notation's `measuresPerLine`.

## Drawing

A view draws a `MeasureLine` of a track and derives what it draws from the events: a note's onset across its measure, its heads and ledgers from the track's `StaffWindow`, and its value from its beats. A drum draws as its heads alone, and a rest draws nothing.
