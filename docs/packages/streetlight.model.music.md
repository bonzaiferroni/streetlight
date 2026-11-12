# Package streetlight.model.music

## Introduction

The domain model of music: songs, their tracks, and the sections and notes those hold. It is the source of truth that storage keeps and playback reads.

Notation is the written representation of music. Its forms live in the subpackages: `streetlight.model.music.text` reads and writes music as text, and `streetlight.model.music.ui` lays it out for a UI to draw. This package depends on neither.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.data` | `RecordId` |

## Tracks

`Music.sections` holds the sections of a music, each a `SectionId` and a name no other section of the music holds. A section is identified by its id everywhere in the model, so a rename changes one name and every reference follows. Each track holds what it plays for a section, and two tracks play the same section by holding the same id.

A `Track` is identified by its `TrackId` everywhere in the model and shown under its `name`, which no other track of its song holds, so a rename changes one name and every reference follows. What a track plays for a section is identified by the two ids together, as a `TrackSectionId`.

`TrackType` mirrors the sealed types of `Track`, one entry each, and holds what stands for the type wherever it is shown: its icon and its `Palette` color. A track gives its entry as `trackType`, and a song's markdown names a track's type by the entry's name.

`Music.arrangement` lists the sections every track follows, in order. A `Track` holds its `sections` and its `arrangements`, each an `Arrangement` naming a section built of other sections or arrangements, so content repeated within a section is written once. `sectionsOf` resolves an id into the sections it stands for.

A section lasts as long as the longest track that defines it. A track repeats a shorter section as many whole times as fit, and rests through a section it lacks or what is left after its repeats.

Sections play end to end, so an onset is the running sum of the lengths before it and is never stored. The same holds within a `Progression`: each `ChordNote` carries its `beats`, a `null` chord is a rest, and the progression's length is the sum of its notes.

A section may open with a `pickup`, notes that sound at the end of the measure before it. A pickup is shorter than a measure and does not count toward the section's length, and an arrangement takes the pickup of its first section. A section's `measuresPerLine` is the layout of its text, read from the measures on its first line.

A melody note's `Interval` and a chord's root count from the tonic of the key, so a change of key transposes every track. The key and every track are written as played, and sound `Music.capo` semitones higher, as chord shapes behind a guitar capo. A track's own `capo` adds to it, and a `null` capo counts as none. A track's text opens with its properties, such as `-capo: 2`, before its first heading.

A scale degree counts from the `Key.majorTonic`, the tonic of the major scale the key's mode is drawn from, so a tune in A minor writes A as `6`.

## Lyrics

Lyrics are tied to a melody by timing alone, with no reference between the tracks. The first lyric track is sung to every melody track, and a stanza belongs to the section the melody plays as the stanza starts, so the verses of a melody played three times are its three lines. A phrase that names its `lyrics` is sung only the stanzas it names, still placed by timing. `lyricLinesOf` gives those lines, one for each time a track plays a section.

## Performance

The domain model holds the composition alone. A performance of it, such as a `Clip` recorded of a track's section, is its own type, kept beside a song and never on a `Track`, so it stays out of song files.

A `Clip` is a recording with its own identity, carrying only what decides where it fits: tempo, time, bars and the key it sounds in, capo already applied. A `SectionClipId` binds one to a track's section by its `TrackSectionId`, so a clip whose properties line up can be placed elsewhere.

A `Mix` holds those bindings for one music, and the sections it hides from its overview, each a `TrackSectionId`, since tracks share a section's id. It is seeded with the music's tempo and sounding key and keeps its own after, so a change to the music leaves its clips fitting the mix they were recorded for. A music has many mixes; one that needs a different composition is a copy of the music.
