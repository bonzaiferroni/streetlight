# Package streetlight.model.music.text

## Introduction

The text form of notation, the one a user edits: the parsers that translate between text and the domain model of `streetlight.model.music`.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.music` | The domain model |

## Section Names

Text names a section, and the model identifies it by id. A read resolves each name through a `SectionNames` of the music's sections, which gives a name no section holds a new one, and the music takes the sections back once every text of it has been read. A write names each id the same way.

## Track Notations

Each track type has a text notation, read and written by its `FooParser`, as `ChordParser`. The parsers share the ambient functions of `TrackNotation.kt`, which work on text, beats and ticks and know nothing of notes. The layout every notation shares is a heading per section, a line ending in `:`, then its lines of measures, and an arrangement on a line of its own, as `Song: VerseA VerseB`. The notes before a section's first bar line are its pickup. A property is a labeled line led by `-`, which holds no bar line, so its label never collides with a section's name. It belongs to the section whose heading it follows, after its measures when written, or to the track when it follows no heading. A read reports each problem by line and skips what it cannot read, so a partly written text still gives a track.

| Track | Notation | Lengths |
|---|---|---|
| `ChordTrack` | Chord symbols in the `NotationStyle` | A measure's tokens share it evenly; a pickup token is a pulse, the beat a tempo counts |
| `LyricTrack` | Syllables and words, a syllable keeping its `-`, with `_` to hold and `0` to rest | As melody, its own clock bound to the melody only by timing |
| `MelodyTrack` | Jianpu, with `\` after a note for each halving of its length | Each note carries its own, a plain note a quarter in every meter; a measure that does not add up is reported |
| `DrumTrack` | ASCII drum tab, a row per drum labeled before its first bar line | A measure's characters share it evenly; a hit rings until the next |

Each parser also reads and writes one section alone, as the lines of its measures with no heading, property or arrangement. `readSection` and `writeSection` choose the parser by the section's type, and a section read keeps what its notation does not state. The track's whole text is for a song's markdown.

A lyric track read against a melody is checked by section: each syllable starts on a melody note, and the stanza ends with the phrase or before.

| Property | Belongs to | States |
|---|---|---|
| `-capo: 2` | A track | The track's own capo |
| `-lyrics: VerseB VerseC` | A melody section | The stanzas that may be sung to it, any when left out and none when empty |

A notation that names absolute pitches, such as letter chords, adds the tonic when written and subtracts it when read.

## Song Markdown

`SongMarkdown` carries a whole song as one markdown document built from the same notations, with a heading per track giving its name and type. Text names a track and never states its id, so a read gives each track a new one. A read refuses only a document whose structure it cannot follow; the notation within is read as leniently as an editor reads it. `docs/songs/amazing-grace.md` is an example.
