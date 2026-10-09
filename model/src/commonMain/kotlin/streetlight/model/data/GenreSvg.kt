package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each genre of music. */
enum class GenreSvg(val svg: SvgPack) {
    // Rock
    Rock(SvgFile.GuitarPick),
    HardRock(SvgFile.Bolt),
    Metal(SvgFile.Skull),
    Punk(SvgFile.Bomb),
    Grunge(SvgFile.MusicBolt),
    Alternative(SvgFile.ArrowsShuffle),
    Indie(SvgFile.Vinyl),
    Emo(SvgFile.HeartBroken),
    Psychedelic(SvgFile.Mushroom),
    Surf(SvgFile.Ripple),

    // Pop
    Pop(SvgFile.Star),
    KPop(SvgFile.Magic),
    Disco(SvgFile.Confetti),
    Synthpop(SvgFile.WaveSawTool),

    // Hip Hop & R&B
    HipHop(SvgFile.Microphone),
    Rap(SvgFile.MicrophoneHandheld),
    Trap(SvgFile.Speakerphone),
    RnB(SvgFile.Heart),
    Soul(SvgFile.Fire),
    Funk(SvgFile.Sunglasses),

    // Electronic
    Electronic(SvgFile.WaveSquare),
    House(SvgFile.Home),
    Techno(SvgFile.WaveSawTool),
    Dubstep(SvgFile.WavesElectricity),
    DrumAndBass(SvgFile.Metronome),
    Ambient(SvgFile.Cloud),
    LoFi(SvgFile.Headphones),

    // Jazz & Blues
    Jazz(SvgFile.Moon),
    Swing(SvgFile.Shoe),
    BigBand(SvgFile.Social),
    Blues(SvgFile.CloudRain),

    // Roots
    Country(SvgFile.Cactus),
    Folk(SvgFile.Campfire),
    Bluegrass(SvgFile.GuitarPick),
    Americana(SvgFile.Flag),
    Celtic(SvgFile.Clover),
    SingerSongwriter(SvgFile.Writing),
    Acoustic(SvgFile.GuitarPick),

    // Latin & World
    Latin(SvgFile.Pepper),
    Salsa(SvgFile.Pepper),
    Reggaeton(SvgFile.Fire),
    Reggae(SvgFile.Leaf),
    World(SvgFile.World),
    Afrobeat(SvgFile.Metronome),

    // Classical & Stage
    Classical(SvgFile.Clef),
    Opera(SvgFile.MasksTheater),
    MusicalTheater(SvgFile.MasksTheater),
    Choral(SvgFile.UsersGroup),
    Soundtrack(SvgFile.Movie),
    Instrumental(SvgFile.Piano),

    // Spiritual
    Gospel(SvgFile.Pray),
    Worship(SvgFile.Cross),

    // Other
    Experimental(SvgFile.Flask),
    JamBand(SvgFile.UsersGroup),
    Covers(SvgFile.Copy),
    Tribute(SvgFile.Award),
    Kids(SvgFile.HorseToy),
    Holiday(SvgFile.Gift),
    Comedy(SvgFile.MoodCrazyHappy),
    SpokenWord(SvgFile.Quote),
    Poetry(SvgFile.Feather)
}
