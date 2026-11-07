# Package streetlight.model.music.text

## Introduction

The text form of notation, the one a user edits: the parsers that translate between text and the domain model of `streetlight.model.music`.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.music` | The domain model |

## Track Notations

Each track type has a text notation, read and written by its `FooParser`, as `ChordParser`. The parsers share the ambient functions of `TrackNotation.kt`, which work on text, beats and ticks and know nothing of notes. The layout every notation shares is a heading per section, a line ending in `:`, then its lines of measures, or the line of sections it arranges. The notes before a section's first bar line are its pickup. A read reports each problem by line and skips what it cannot read, so a partly written text still gives a track.

| Track | Notation | Lengths |
|---|---|---|
| `ChordTrack` | Chord symbols in the `NotationStyle` | A measure's tokens share it evenly; a pickup token is a beat |
| `LyricTrack` | Syllables and words, a syllable keeping its `-`, with `_` to hold and `0` to rest | As melody, its own clock bound to the melody only by timing |
| `MelodyTrack` | Jianpu, with `\` after a note for each halving of its length | Each note carries its own, a plain note a quarter in every meter; a measure that does not add up is reported |
| `DrumTrack` | ASCII drum tab, a row per drum labeled before its first bar line | A measure's characters share it evenly; a hit rings until the next |

A lyric track read against a melody is checked by section: each syllable starts on a melody note, and the stanza ends with the phrase or before.

A notation that names absolute pitches, such as letter chords, adds the tonic when written and subtracts it when read.
