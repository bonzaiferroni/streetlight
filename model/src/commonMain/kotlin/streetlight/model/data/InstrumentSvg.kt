package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of instrument. */
enum class InstrumentSvg(val svg: SvgPack) {
    // Plucked Strings
    Guitar(SvgFile.AcousticGuitar),
    AcousticGuitar(SvgFile.AcousticGuitar),
    ElectricGuitar(SvgFile.ElectricGuitar),
    BassGuitar(SvgFile.ElectricGuitar),
    Ukulele(SvgFile.AcousticGuitar),
    Banjo(SvgFile.AcousticGuitar),
    Mandolin(SvgFile.AcousticGuitar),
    Harp(SvgFile.Harp),
    Sitar(SvgFile.AcousticGuitar),

    // Bowed Strings
    Violin(SvgFile.Violin),
    Fiddle(SvgFile.Violin),
    Viola(SvgFile.Violin),
    Cello(SvgFile.Violin),
    DoubleBass(SvgFile.Violin),

    // Keys
    Piano(SvgFile.Piano),
    Keyboard(SvgFile.Synthesizer),
    Organ(SvgFile.Synthesizer),
    Accordion(SvgFile.Accordion),
    Synthesizer(SvgFile.Synthesizer),

    // Percussion
    Drums(SvgFile.Drum),
    Percussion(SvgFile.Drum),
    HandDrum(SvgFile.Drum),
    Cajon(SvgFile.Box),
    Tambourine(SvgFile.Crosshairs),
    Cymbals(SvgFile.Disc),
    Xylophone(SvgFile.Piano),
    Bells(SvgFile.Bell),
    Triangle(SvgFile.Triangle),

    // Woodwinds
    Flute(SvgFile.Flute),
    Clarinet(SvgFile.Flute),
    Oboe(SvgFile.Flute),
    Saxophone(SvgFile.Saxophone),
    Harmonica(SvgFile.WaveSquare),
    Bagpipes(SvgFile.WaveSine),

    // Brass
    Trumpet(SvgFile.Trumpet),
    Trombone(SvgFile.Trumpet),
    FrenchHorn(SvgFile.Trumpet),
    Tuba(SvgFile.Trumpet),

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
