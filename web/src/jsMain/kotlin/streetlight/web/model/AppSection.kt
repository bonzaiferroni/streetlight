package streetlight.web.model

import streetlight.model.ui.Screen

/** A section of the app, each delivered in its own bundle chunk. */
enum class AppSection {
    /** Viewing content, delivered in the main bundle. */
    Core,

    /** Sharing and configuring content. */
    Studio,
}

/** The [AppSection] that holds the screen. */
fun Screen.toAppSection() = when (this) {
    Screen.Home,
    Screen.SongProfile,
    Screen.Contribute,
    Screen.Connect,
    Screen.TalentProfile,
    Screen.Star,
    Screen.PasswordReset,
    Screen.Location,
    Screen.Galaxy,
    Screen.GalaxyFeed,
    Screen.Event,
    Screen.AboutApp,
    Screen.Earth,
    Screen.City,
    Screen.CityFeed,
    Screen.Media,
    Screen.Offering,
    Screen.Error -> AppSection.Core

    Screen.Onboard,
    Screen.Sandbox,
    Screen.SiteMonitor,
    Screen.Chat,
    Screen.Feedback,
    Screen.VerifyEmail,
    Screen.AccountNotOwned,
    Screen.AccountLockdown,
    Screen.ActionReport,
    Screen.Docs,
    Screen.Talk,
    Screen.StarDash,
    Screen.Inbox,
    Screen.MediaUpdate,
    Screen.ProfileConfig,
    Screen.StarConfig,
    Screen.LocationConfig,
    Screen.LocationScout,
    Screen.LocationUpdate,
    Screen.GalaxyFoundry,
    Screen.GalaxyConfig,
    Screen.UpdateEvent,
    Screen.EventScout,
    Screen.MediaForge,
    Screen.SiteConfig,
    Screen.CityConfig -> AppSection.Studio
}
