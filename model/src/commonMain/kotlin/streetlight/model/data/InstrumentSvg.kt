package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of instrument. */
enum class InstrumentSvg(val svg: SvgPack) {
    // Plucked Strings
    Guitar(SvgFile.GuitarPick),
    AcousticGuitar(SvgFile.GuitarPick),
    ElectricGuitar(SvgFile.MusicBolt),
    BassGuitar(SvgFile.GuitarPick),
    Ukulele(SvgFile.GuitarPick),
    Banjo(SvgFile.GuitarPick),
    Mandolin(SvgFile.GuitarPick),
    Harp(SvgFile.Music),
    Sitar(SvgFile.GuitarPick),

    // Bowed Strings
    Violin(SvgFile.MusicStar),
    Fiddle(SvgFile.MusicStar),
    Viola(SvgFile.MusicStar),
    Cello(SvgFile.MusicStar),
    DoubleBass(SvgFile.MusicStar),

    // Keys
    Piano(SvgFile.Piano),
    Keyboard(SvgFile.Keyboard),
    Organ(SvgFile.Piano),
    Accordion(SvgFile.Piano),
    Synthesizer(SvgFile.WaveSawTool),

    // Percussion
    Drums(SvgFile.Metronome),
    Percussion(SvgFile.Metronome),
    HandDrum(SvgFile.Metronome),
    Cajon(SvgFile.Box),
    Tambourine(SvgFile.Crosshairs),
    Cymbals(SvgFile.Disc),
    Xylophone(SvgFile.Piano),
    Bells(SvgFile.Bell),
    Triangle(SvgFile.Triangle),

    // Woodwinds
    Flute(SvgFile.WaveSine),
    Clarinet(SvgFile.WaveSine),
    Oboe(SvgFile.WaveSine),
    Saxophone(SvgFile.WaveSine),
    Harmonica(SvgFile.WaveSquare),
    Bagpipes(SvgFile.WaveSine),

    // Brass
    Trumpet(SvgFile.Speakerphone),
    Trombone(SvgFile.Speakerphone),
    FrenchHorn(SvgFile.Speakerphone),
    Tuba(SvgFile.Speakerphone),

    // Voice
    Vocals(SvgFile.MicrophoneHandheld),
    BackingVocals(SvgFile.Microphone),
    Choir(SvgFile.UsersGroup),
    Beatbox(SvgFile.WaveSine),

    // Electronic
    Turntables(SvgFile.Vinyl),
    DjController(SvgFile.AdjustmentsHorizontal),
    DrumMachine(SvgFile.DeviceSpeaker),
    Sampler(SvgFile.LayoutGrid),
    Laptop(SvgFile.DeviceLaptop),
    Looper(SvgFile.Repeat),
    Theremin(SvgFile.WavesElectricity)
}
