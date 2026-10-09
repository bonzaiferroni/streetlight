package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of post. */
enum class PostSvg(val svg: SvgPack) {
    Event(SvgFile.Calendar),
    Location(SvgFile.MapPin),
    Media(SvgFile.Photo),
    Announcement(SvgFile.Speakerphone),
    Update(SvgFile.Refresh),
    Alert(SvgFile.AlertTriangle),
    Question(SvgFile.Question),
    Discussion(SvgFile.Messages),
    Review(SvgFile.Star),
    Recommendation(SvgFile.ThumbUp),
    Poll(SvgFile.ChartBar),
    Offer(SvgFile.Tag),
    Job(SvgFile.Briefcase),
    Volunteer(SvgFile.HeartHandshake),
    LostAndFound(SvgFile.Search)
}
