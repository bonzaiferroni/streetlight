package streetlight.model.studio

/** The songs a user keeps on the device, with their music, tracks, mixes, clips and studies, one DAO for each. */
interface StudioDb {
    fun songDao(): SongDao
    fun musicDao(): MusicDao
    fun trackDao(): TrackDao
    fun mixDao(): MixDao
    fun sectionClipDao(): SectionClipDao
    fun clipDao(): ClipDao
    fun studyDao(): StudyDao
}
