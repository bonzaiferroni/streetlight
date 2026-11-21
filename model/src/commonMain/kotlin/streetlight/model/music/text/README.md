# Package streetlight.model.music.text

## Introduction

The text form of notation, the one a user edits: the parsers that translate between text and the domain model of `streetlight.model.music`.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.music` | The songs and tracks its parsers read and write |

## Names

Text names a section, a notation and a track, and the model identifies each by id. A list of names is separated by commas, so a name holds no comma.

The music's text declares its sections in its arrangement, an indented line for each section it plays, its name and measures where it first plays and its name alone after:

```
Arrangement:
    Verse: 16
    Chorus: 8
    Verse
```

A track's text names only the sections its music declares, found through a `SectionNames` of them. A notation's name is local to its track.

## Track Notations

Each track type has a text notation, read and written by its `FooParser`, as `ChordParser`. The parsers share the ambient functions of `TrackNotation.kt`, which work on text, beats and ticks and know nothing of notes. The layout every notation shares is a heading per notation, a line ending in `:`, then its lines of measures, and an arrangement on a line of its own, as `Song: Verse A, Verse B`. Measures before the first heading are the track's one unnamed notation. The notes before a notation's first bar line are its pickup. A property is a labeled line led by `-`, which holds no bar line, so its label never collides with a name. The track's properties come before its first notation, and a notation's or arrangement's follow its lines. A read reports each problem by line and skips what it cannot read, so a partly written text still gives a track.

| Track | Notation | Lengths |
|---|---|---|
| `ChordTrack` | Chord symbols in the `NotationStyle` | A measure's tokens share it evenly; a pickup token is a pulse, the beat a tempo counts |
| `LyricTrack` | Syllables and words, a syllable keeping its `-`, with `_` to hold and `0` to rest | As melody, sung with the melody's phrase of the same section |
| `MelodyTrack` | Jianpu, with `\` after a note for each halving of its length | Each note carries its own, a plain note a quarter in every meter; a measure that does not add up is reported |
| `DrumTrack` | ASCII drum tab, a row per drum labeled before its first bar line | A measure's characters share it evenly; a hit rings until the next |

A notation or arrangement plays the sections its `-sections:` names. Without one, a notation no arrangement holds plays the section of its own name, and an empty `-sections:` plays none. A section played by two of a track's notations is reported and kept by the first.

Each parser also reads and writes one notation alone, as the lines of its measures with no heading, property or arrangement. `notationParseOf` and `notationTextOf` choose the parser by the notation's type, and a notation read keeps its id and name. The track's whole text is for a song's markdown.

A lyric track read against a melody is checked by section: each syllable starts on a note of the phrase the melody plays there, and the stanza ends with the phrase or before.

| Property | Belongs to | States |
|---|---|---|
| `-capo: 2` | A track | The track's own capo |
| `-lyrics: Lyrics` | A melody track | The lyric track sung with it, the first of its music when left out |
| `-sections: Verse A, Verse B` | A notation or arrangement | The sections it plays |

A notation that names absolute pitches, such as letter chords, adds the tonic when written and subtracts it when read.

## Song Markdown

`SongMarkdown` carries a whole song as one markdown document built from the same notations, with a heading per track giving its name and type. Text names a track and never states its id, so a read gives each track a new one. A read refuses only a document whose structure it cannot follow; the notation within is read as leniently as an editor reads it. `docs/songs/amazing-grace.md` is an example.
