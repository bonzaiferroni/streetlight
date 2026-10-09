package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of [ExtraLink], by what it links to. */
enum class ExtraLinkSvg(val svg: SvgPack) {
    // General
    Website(SvgFile.World),
    Tickets(SvgFile.Ticket),
    Rsvp(SvgFile.CalendarCheck),
    Registration(SvgFile.ClipboardCheck),
    Calendar(SvgFile.Calendar),
    Map(SvgFile.Map),
    Directions(SvgFile.Directions),
    Menu(SvgFile.ToolsKitchen),
    Shop(SvgFile.ShoppingBag),
    Merch(SvgFile.Shirt),
    Donate(SvgFile.HeartDollar),
    Petition(SvgFile.Signature),
    Survey(SvgFile.Forms),
    Newsletter(SvgFile.News),
    Blog(SvgFile.Article),
    Email(SvgFile.Mail),
    Phone(SvgFile.Phone),
    Download(SvgFile.Download),
    Document(SvgFile.FileText),
    Pdf(SvgFile.FileTypePdf),

    // Media
    Video(SvgFile.Video),
    Audio(SvgFile.Headphones),
    Music(SvgFile.Music),
    Podcast(SvgFile.MicrophoneHandheld),
    Livestream(SvgFile.Broadcast),
    Photos(SvgFile.Photo),

    // Social
    Facebook(SvgFile.BrandFacebook),
    Instagram(SvgFile.BrandInstagram),
    X(SvgFile.BrandX),
    Threads(SvgFile.BrandThreads),
    Bluesky(SvgFile.BrandBluesky),
    Mastodon(SvgFile.BrandMastodon),
    TikTok(SvgFile.BrandTiktok),
    Snapchat(SvgFile.BrandSnapchat),
    LinkedIn(SvgFile.BrandLinkedin),
    Pinterest(SvgFile.BrandPinterest),
    Reddit(SvgFile.BrandReddit),
    Tumblr(SvgFile.BrandTumblr),
    Discord(SvgFile.BrandDiscord),
    Telegram(SvgFile.BrandTelegram),
    WhatsApp(SvgFile.BrandWhatsapp),
    Signal(SvgFile.BrandSignal),
    Meetup(SvgFile.BrandMeetup),
    Linktree(SvgFile.BrandLinktree),

    // Video & Audio
    YouTube(SvgFile.BrandYoutube),
    Vimeo(SvgFile.BrandVimeo),
    Twitch(SvgFile.BrandTwitch),
    Kick(SvgFile.BrandKick),
    Rumble(SvgFile.BrandRumble),
    Spotify(SvgFile.BrandSpotify),
    Bandcamp(SvgFile.BrandBandcamp),
    SoundCloud(SvgFile.BrandSoundcloud),
    ApplePodcasts(SvgFile.BrandApplePodcast),
    Audible(SvgFile.BrandAudible),

    // Support
    Patreon(SvgFile.BrandPatreon),
    Kickstarter(SvgFile.BrandKickstarter),
    PayPal(SvgFile.BrandPaypal),
    CashApp(SvgFile.BrandCashapp),
    Etsy(SvgFile.BrandEtsy),

    // Reference
    GitHub(SvgFile.Github),
    Wikipedia(SvgFile.BrandWikipedia),
    GoogleMaps(SvgFile.BrandGoogleMaps),
    Tripadvisor(SvgFile.BrandTripadvisor),
    Letterboxd(SvgFile.BrandLetterboxd),
    Behance(SvgFile.BrandBehance),
    Dribbble(SvgFile.BrandDribbble),
    Flickr(SvgFile.BrandFlickr),
    Medium(SvgFile.BrandMedium),
    Strava(SvgFile.BrandStrava)
}
