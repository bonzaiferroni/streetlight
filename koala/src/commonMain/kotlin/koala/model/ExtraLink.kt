package koala.model

import kampfire.model.Url
import koala.SvgFile
import koala.SvgPack
import kotlinx.serialization.Serializable

/** A labeled link a record shows, such as to its tickets or a video. */
@Serializable
data class ExtraLink(
    val label: String,
    val url: Url,
) {
    private val urlIsValid: Boolean
        get() {
            val trimmed = url.value.trim()
            val regex = Regex(
                pattern = """^(https?)://([a-zA-Z0-9\-._~%]+)(:\d+)?(/\S*)?$"""
            )
            return regex.matches(trimmed)
        }
    /** True when the link has a label and a well-formed absolute URL. */
    val isValid get() = label.isNotBlank() && urlIsValid

    companion object {
        val Empty get () = ExtraLink("", Url(""))
    }
}

/** The host of [url] without a leading `www.`, or `null`. */
fun domainOf(url: String): String? =
    Regex("""^(?:[a-zA-Z][a-zA-Z\d+\-.]*://)?(?:[^@/\n]+@)?([^:/\n?#]+)""")
        .find(url.trim())
        ?.groupValues?.get(1)
        ?.removePrefix("www.")
        ?.takeIf { it.isNotBlank() }

fun ExtraLink.toSvg() = flairOf(label, url.value)

/** The flair of the label [text], or of the domain of [href], or a link icon. */
fun flairOf(text: String?, href: String): SvgPack =
    text?.lowercase()?.let { labelFlairs[it] }  ?: domainOf(href)?.let { domainFlairs[it] } ?: SvgFile.Link

private val domainFlairs = mapOf(
    "podcasts.apple.com" to SvgFile.BrandApplePodcast,
    "audible.com" to SvgFile.BrandAudible,
    "bandcamp.com" to SvgFile.BrandBandcamp,
    "behance.net" to SvgFile.BrandBehance,
    "bsky.app" to SvgFile.BrandBluesky,
    "cash.app" to SvgFile.BrandCashapp,
    "discord.com" to SvgFile.BrandDiscord,
    "discord.gg" to SvgFile.BrandDiscord,
    "dribbble.com" to SvgFile.BrandDribbble,
    "etsy.com" to SvgFile.BrandEtsy,
    "facebook.com" to SvgFile.BrandFacebook,
    "fb.me" to SvgFile.BrandFacebook,
    "flickr.com" to SvgFile.BrandFlickr,
    "github.com" to SvgFile.Github,
    "maps.google.com" to SvgFile.BrandGoogleMaps,
    "maps.app.goo.gl" to SvgFile.BrandGoogleMaps,
    "instagram.com" to SvgFile.BrandInstagram,
    "kick.com" to SvgFile.BrandKick,
    "kickstarter.com" to SvgFile.BrandKickstarter,
    "letterboxd.com" to SvgFile.BrandLetterboxd,
    "linkedin.com" to SvgFile.BrandLinkedin,
    "linktr.ee" to SvgFile.BrandLinktree,
    "mastodon.social" to SvgFile.BrandMastodon,
    "medium.com" to SvgFile.BrandMedium,
    "meetup.com" to SvgFile.BrandMeetup,
    "patreon.com" to SvgFile.BrandPatreon,
    "paypal.com" to SvgFile.BrandPaypal,
    "paypal.me" to SvgFile.BrandPaypal,
    "pinterest.com" to SvgFile.BrandPinterest,
    "reddit.com" to SvgFile.BrandReddit,
    "rumble.com" to SvgFile.BrandRumble,
    "signal.org" to SvgFile.BrandSignal,
    "signal.me" to SvgFile.BrandSignal,
    "snapchat.com" to SvgFile.BrandSnapchat,
    "soundcloud.com" to SvgFile.BrandSoundcloud,
    "open.spotify.com" to SvgFile.BrandSpotify,
    "spotify.com" to SvgFile.BrandSpotify,
    "strava.com" to SvgFile.BrandStrava,
    "t.me" to SvgFile.BrandTelegram,
    "telegram.org" to SvgFile.BrandTelegram,
    "threads.net" to SvgFile.BrandThreads,
    "threads.com" to SvgFile.BrandThreads,
    "tiktok.com" to SvgFile.BrandTiktok,
    "tripadvisor.com" to SvgFile.BrandTripadvisor,
    "tumblr.com" to SvgFile.BrandTumblr,
    "twitch.tv" to SvgFile.BrandTwitch,
    "vimeo.com" to SvgFile.BrandVimeo,
    "wa.me" to SvgFile.BrandWhatsapp,
    "whatsapp.com" to SvgFile.BrandWhatsapp,
    "en.wikipedia.org" to SvgFile.BrandWikipedia,
    "wikipedia.org" to SvgFile.BrandWikipedia,
    "x.com" to SvgFile.BrandX,
    "twitter.com" to SvgFile.BrandX,
    "youtube.com" to SvgFile.BrandYoutube,
    "m.youtube.com" to SvgFile.BrandYoutube,
    "youtu.be" to SvgFile.BrandYoutube,
    "music.youtube.com" to SvgFile.BrandYoutube,
)

internal val labelFlairs = mapOf(
    "tickets" to SvgFile.Ticket,
    "edit" to SvgFile.Edit,
    "website" to SvgFile.World,
    "calendar" to SvgFile.Calendar,
)
