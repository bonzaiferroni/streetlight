package streetlight.model.studio

/** The songs a user keeps on the device, with their music, tracks, mixes, clips and studies, one DAO for each. */
interface StudioDb {
    val song: SongDao
    val music: MusicDao
    val track: TrackDao
    val mix: MixDao
    val sectionClip: SectionClipDao
    val clip: ClipDao
    val study: StudyDao
}
