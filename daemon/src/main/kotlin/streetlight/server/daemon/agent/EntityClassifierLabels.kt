package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import kampfire.model.Labeled
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.toDataOr
import streetlight.model.data.EventSubtype
import streetlight.model.data.EventType
import kotlin.enums.EnumEntries

/** A short description of this type, embedded for [EntityClassifier] to compare events against. */
fun EventType.getClassifierLabel(): String = when (this) {
    EventType.Meetup -> "A casual gathering of people with a shared interest"
    EventType.Music -> "An event centered on live music"
    EventType.Church -> "A religious service or faith community gathering"
    EventType.Fitness -> "A workout or physical fitness activity"
    EventType.Education -> "A class, workshop or lecture for learning"
    EventType.Comedy -> "A stand-up, improv or sketch comedy show"
    EventType.Dance -> "A dance party, social dance or dance class"
    EventType.Sports -> "A sports game, match or tournament"
    EventType.Volunteer -> "A volunteer opportunity or community service project"
    EventType.Youth -> "An event for kids, teens or families"
    EventType.Arts -> "A theater, film, gallery or art event"
    EventType.FoodAndDrink -> "A tasting, food truck, special dinner or drink event"
}

/** A short description of this subtype, embedded for [EntityClassifier] to compare events against. */
fun EventSubtype.getClassifierLabel(): String = when (this) {
    EventSubtype.Picnic -> "An outdoor meal shared in a park"
    EventSubtype.Potluck -> "A shared meal where each guest brings a dish"
    EventSubtype.Networking -> "A professional networking event"
    EventSubtype.BookClub -> "A book club meeting to discuss a book"
    EventSubtype.Trivia -> "A trivia or quiz night"
    EventSubtype.GameNight -> "A night of board games, card games or bingo"
    EventSubtype.Concert -> "A live music performance or concert"
    EventSubtype.OpenMic -> "An open mic where anyone can sign up to perform"
    EventSubtype.StreetPerformance -> "A performance by buskers in a public space"
    EventSubtype.DJSet -> "A DJ set or electronic dance music night"
    EventSubtype.Hike -> "A group walk or hike on a trail"
    EventSubtype.Exercise -> "A group workout or exercise session"
    EventSubtype.Meditation -> "A guided meditation, sound bath or breathwork session"
    EventSubtype.Class -> "A class or workshop teaching a skill"
    EventSubtype.Lecture -> "A talk, lecture or author event"
    EventSubtype.SportsMatch -> "A professional or college sports game to watch"
    EventSubtype.PickupGame -> "A drop-in, pickup or league game of a recreational sport to play"
    EventSubtype.Theater -> "A play, musical or stage performance"
    EventSubtype.Film -> "A film screening or movie night"
    EventSubtype.ArtExhibition -> "An art exhibition, gallery opening or art show"
    EventSubtype.Fundraiser -> "A fundraiser or benefit for a cause"
    EventSubtype.FoodTruck -> "A food truck serving at a venue"
    EventSubtype.Festival -> "A festival or fair with many attractions"
}
