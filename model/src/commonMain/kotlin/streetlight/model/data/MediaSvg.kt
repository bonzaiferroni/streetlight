package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of media. */
enum class MediaSvg(val svg: SvgPack) {
    Text(SvgFile.FileText),
    Image(SvgFile.Photo),
    Gallery(SvgFile.LibraryPhoto),
    Link(SvgFile.Link),
    Video(SvgFile.Video),
    Audio(SvgFile.Headphones),
    Song(SvgFile.Music),
    Podcast(SvgFile.MicrophoneHandheld),
    Livestream(SvgFile.Broadcast),
    News(SvgFile.News),
    Document(SvgFile.File),
    Poll(SvgFile.ChartBar),
    Quote(SvgFile.Quote),
    Embed(SvgFile.Code),
    File(SvgFile.Paperclip)
}
