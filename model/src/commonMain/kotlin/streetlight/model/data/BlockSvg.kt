package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of layout block. */
enum class BlockSvg(val svg: SvgPack) {
    Header(SvgFile.LayoutNavbar),
    Footer(SvgFile.LayoutBottombar),
    Heading(SvgFile.Heading),
    Text(SvgFile.LetterT),
    RichText(SvgFile.Typography),
    Quote(SvgFile.Quote),
    List(SvgFile.List),
    Image(SvgFile.Photo),
    Gallery(SvgFile.LibraryPhoto),
    Carousel(SvgFile.CarouselHorizontal),
    Video(SvgFile.Video),
    Audio(SvgFile.Music),
    Embed(SvgFile.Code),
    Button(SvgFile.Click),
    Link(SvgFile.Link),
    Links(SvgFile.LayoutMinimal),
    Divider(SvgFile.Separator),
    Spacer(SvgFile.SpacingVertical),
    Table(SvgFile.Table),
    Form(SvgFile.Forms),
    Tabs(SvgFile.AppWindow),
    Columns(SvgFile.Columns),
    Accordion(SvgFile.LayoutRow),
    Comments(SvgFile.Messages),
    Posts(SvgFile.News),
    Events(SvgFile.Calendar),
    Calendar(SvgFile.Calendar),
    Countdown(SvgFile.Hourglass),
    Map(SvgFile.Map),
    Location(SvgFile.MapPin),
    Hours(SvgFile.Clock),
    Contact(SvgFile.AddressBook),
    Social(SvgFile.Share),
    Donate(SvgFile.HeartDollar),
    Tickets(SvgFile.Ticket)
}
