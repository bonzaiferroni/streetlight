# Package streetlight.model.music

## Introduction

The domain model of music: songs, their tracks, and the sections and notes those hold. It is the source of truth that storage keeps and playback reads.

Notation is the written representation of music. Its forms live in the subpackages: `streetlight.model.music.text` reads and writes music as text, and `streetlight.model.music.ui` lays it out for a UI to draw. This package depends on neither.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.data` | `RecordId` |

## Records

A `Song` has many `Music`, each one way of playing and understanding it, under a name of its own that starts as the song's title. A `Track` belongs to one music by its `musicId`, and `Music.trackOrder` only orders them: a track it does not name goes after those it does, and an id no track holds is skipped. A song's markdown orders them by its track headings. Songs, musics, tracks, track clips, clips and studies are each kept as separate records, so a reference between them is an id. `MusicSheet` joins a music to its tracks where the whole is edited, played or exported, and `SongMusic` joins it to its song where musics are listed.

A `SectionId` or `NotationId` is not a record. A reference to a section or a notation outlives what it names unless the edit that removes it clears the reference, so a reader of one skips what the music or track no longer holds.

## Sections

`Music.sections` holds the sections of a music, each a `Section` of a whole number of measures under a name no other section of the music holds. A section is identified by its `SectionId` everywhere in the model, so a rename changes one name and every reference follows. `Music.arrangement` plays the sections in order, and a section may play more than once.

A section's length is its measures, stated by the music and never taken from a track.

## Tracks

A `Track` is identified by its `TrackId` everywhere in the model and shown under its `name`, which no other track of its music holds, so a rename changes one name and every reference follows.

`TrackType` mirrors the sealed types of `Track`, one entry each, and holds what stands for the type wherever it is shown: its icon and its `Palette` color. A track gives its entry as `trackType`, and a song's markdown names a track's type by the entry's name.

A track holds its `notations`, the notes it plays, and its `arrangements`, each an `Arrangement` playing other notations or arrangements in order, so content repeated is written once. Each is identified by a `NotationId` of its own, unique across every track. `notationsOf` resolves an id into the notations it stands for.

`Track.sections` maps each section of the music the track plays to the notation or arrangement it plays there, so one notation may fill many sections and a section holds at most one notation of a track. A section the track does not map is a rest. A track repeats a notation shorter than its section as many whole times as fit, and rests through what is left.

A notation's `name` is shown with it, and need not be unique beyond its track. A track has at most one unnamed notation, which `nameOf` shows by the names of its music and track, as `Amazing Grace Chords`.

Notations play end to end, so an onset is the running sum of the lengths before it and is never stored. The same holds within a `Progression`: each `ChordNote` carries its `beats`, a `null` chord is a rest, and the progression's length is the sum of its notes.

A notation may open with a `pickup`, notes that sound at the end of the measure before it. A pickup is shorter than a measure and does not count toward the notation's length, and an arrangement takes the pickup of its first notation. A notation's `measuresPerLine` is the layout of its text, read from the measures on its first line.

A melody note's `Interval` and a chord's root count from the tonic of the key, so a change of key transposes every track. The key and every track are written as played, and sound `Music.capo` semitones higher, as chord shapes behind a guitar capo. A track's own `capo` adds to it, and a `null` capo counts as none. A track's text opens with its properties, such as `-capo: 2`, before its first notation.

A scale degree counts from the `Key.majorTonic`, the tonic of the major scale the key's mode is drawn from, so a tune in A minor writes A as `6`.

## Lyrics

A melody track is sung with the lyric track its `lyrics` names, or the first lyric track of its music when it names none. A stanza is sung with the phrase its track plays for the same section, and within a section the two are tied by timing alone. `lyricLinesOf` gives the lines sung to a notation, one for each time the track plays it.

## Performance

The domain model holds the composition alone. A performance of it, such as a `Clip` recorded of a track, is its own type, kept beside a song and never on a `Track`, so it stays out of song files.

A `Clip` is a recording with its own identity, carrying only what decides where it fits: tempo, time, bars and the key it sounds in, capo already applied. A `TrackClip` binds one to a notation of a track, or to the whole track when it names no notation, one clip to each, so a clip whose properties line up can be placed elsewhere. A clip fits the music's own tempo and sounding key; one that needs another is bound to a copy of the music.
