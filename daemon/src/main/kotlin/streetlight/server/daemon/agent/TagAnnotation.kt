package streetlight.server.daemon.agent

import streetlight.model.data.EventTag

/**
 * A short description of this tag, embedded for [EntityClassifier] to compare events against, and the guide to
 * annotating events with it.
 */
fun EventTag.getClassifierLabel(): String = when (this) {
    // Broad. A narrow tag implies a broad tag only where its rule says "Also"; otherwise an event takes the broad tag
    // when it is about that area.

    // Any gathering of people around a shared interest, open to newcomers. Not for a show, class or game.
    EventTag.Meetup -> "A social gathering or meetup of people with a shared interest, such as a club or group"
    // Any event where music is performed or played. A concert, open mic, DJ set or karaoke night is also Music.
    EventTag.Music -> "An event centered on music: concerts, live bands, DJs, jam sessions and sing-alongs"
    // An event a faith community holds or one about faith, whatever its activity. A concert at a church is Church only
    // when the church presents it as part of its community life.
    EventTag.Church -> "A religious service, worship, prayer, Bible study or faith community gathering"
    // Physical activity and care of body or mind. A hike, workout or wellness session is also Health & Fitness.
    EventTag.HealthAndFitness -> "Exercise, yoga, running, hiking, meditation and other health and fitness activities"
    // An event whose purpose is learning. A class or lecture is also Education; crafting is only when taught.
    EventTag.Education -> "A class, workshop, lecture, talk or course where people learn something"
    // Playing or watching a sport. A pickup game or sports match is also Sports.
    EventTag.Sports -> "Playing or watching a sport: games, matches, leagues and pickup play"
    // An event where people give time or money to a cause.
    EventTag.Volunteer -> "A volunteer opportunity, service project, cleanup or fundraiser for a cause"
    // An event made for children or families, such as a story time or kids' class. Not an all-ages event that merely
    // admits children.
    EventTag.KidsAndFamily -> "An event made for kids, teens or families: story times, kids' classes and family days"
    // The performing and visual arts. Theater, film, an exhibition or comedy is also Arts.
    EventTag.Arts -> "Theater, film, visual art, galleries, comedy and other arts performances and exhibitions"
    EventTag.FoodAndDrink -> "An event centered on food or drink: tastings, food trucks, special dinners and beer events"

    // Meetup
    EventTag.Picnic -> "An outdoor meal shared in a park"
    EventTag.Potluck -> "A shared meal where each guest brings a dish"
    // Meeting people for work or business. Not a social meetup with no professional purpose.
    EventTag.Networking -> "A professional networking event, business meetup or industry gathering"
    // A book club, author talk, book signing, writing group or poetry reading.
    EventTag.BooksAndWriting -> "A book club, author talk, writing group, poetry reading or book event"
    // Board games, card games, bingo, trivia and tabletop games. Not video game tournaments of a sport.
    EventTag.GamesAndTrivia -> "A trivia night, bingo, board games, card games or tabletop game night"
    // An event for meeting a partner: speed dating, singles mixers.
    EventTag.Singles -> "A singles mixer, speed dating or event for meeting someone to date"
    // An event for or by the LGBTQ+ community, including drag shows.
    EventTag.LGBTQ -> "An event for the LGBTQ+ community, such as a queer social, pride event or drag show"
    // Technology as its subject: AI, software, cybersecurity, hardware hacking.
    EventTag.Tech -> "A technology meetup or talk about software, AI, cybersecurity or computing"
    // An event for pets and their people.
    EventTag.Pets -> "An event for pets and their owners, such as a dog costume contest or yappy hour"
    // Watching a broadcast together, usually a game at a bar. Also Sports when the broadcast is a game.
    EventTag.WatchParty -> "A watch party to see a game or broadcast together on big screens"

    // Music
    // Artists or bands performing for an audience. A DJ set is a Concert only when billed as a performance.
    EventTag.Concert -> "A live music performance by a band, artist, orchestra or singer"
    // Anyone may sign up to perform: music, comedy or poetry.
    EventTag.OpenMic -> "An open mic where anyone can sign up to perform music, comedy or poetry"
    EventTag.StreetPerformance -> "A performance by buskers or street performers in a public space"
    // A DJ playing for a crowd, often electronic dance music.
    EventTag.DJSet -> "A DJ set, club night or electronic dance music night"
    EventTag.Karaoke -> "A karaoke night where guests sing along to songs"
    // Dancing by the guests: socials, lessons, dance parties. Not a dance performance to watch, which is Theater.
    EventTag.Dance -> "A social dance, dance party or dance lesson such as salsa, swing or line dancing"

    // Health & Fitness
    // Also Nature.
    EventTag.Hike -> "A group hike or walk on a trail"
    // Time spent in the outdoors: birding, paddling, camping, nature walks, stewardship of open space.
    EventTag.Nature -> "An outdoor nature activity: birding, paddling, camping, nature walks and open space visits"
    // A workout, run, ride or climb done as exercise.
    EventTag.Exercise -> "A group workout, run, bike ride, climb or exercise class"
    // Care of mind and body at rest: meditation, sound baths, breathwork. Yoga is Wellness when taught as a practice of
    // calm, and Exercise when taught as a workout; a yoga class may be both.
    EventTag.Wellness -> "A meditation, sound bath, breathwork, yoga or wellness session"

    // Education
    // Taught by an instructor, one session or a series.
    EventTag.Class -> "A class or course where an instructor teaches a skill"
    // Making things by hand: pottery, painting, knitting, jewelry, woodwork. Also Class when taught.
    EventTag.Crafting -> "A crafting session or workshop: pottery, painting, knitting, jewelry or woodwork"
    // A speaker presents to an audience: talks, panels, author events, forums.
    EventTag.Lecture -> "A talk, lecture, panel, presentation or author event"

    // Sports
    // Watching others compete, in person or at a watch party.
    EventTag.SportsMatch -> "A professional or college sports game or match to watch"
    // Playing in a game: pickup, drop-in, leagues, open play.
    EventTag.PickupGame -> "A drop-in, pickup or league game of a recreational sport to play"

    // Arts
    // A live stage performance: plays, musicals, ballet, burlesque, drag shows.
    EventTag.Theater -> "A play, musical, ballet, burlesque or other stage performance"
    // A screening of a film, including film festivals and movie nights.
    EventTag.Film -> "A film screening, movie night or film festival"
    EventTag.ArtExhibition -> "An art exhibition, gallery opening or art show"
    // Stand-up, improv or sketch, performed or taught. Also Arts.
    EventTag.Comedy -> "A stand-up, improv or sketch comedy show"

    // Volunteer
    // Raising money for a cause: galas, benefit nights, charity runs. Also Volunteer.
    EventTag.Fundraiser -> "A fundraiser, gala or benefit raising money for a cause"
    // Picking up trash or restoring a place. Also Volunteer.
    EventTag.Cleanup -> "A volunteer cleanup of a park, river, trail or neighborhood"
    // Service to neighbors in need: food drives, meal service, aid work. Also Volunteer.
    EventTag.CommunityOutreach -> "A community outreach or service project helping neighbors in need"

    // Food & Drink
    // A food truck serving at a venue. Also Food & Drink.
    EventTag.FoodTruck -> "A food truck serving at a brewery or venue"
    // Sampling drinks or food, often paired: beer, wine, cheese, chocolate. Also Food & Drink.
    EventTag.Tasting -> "A tasting or pairing of beer, wine, spirits, cheese or chocolate"

    // General
    // Many acts or attractions over hours or days: fairs, music festivals, film festivals.
    EventTag.Festival -> "A festival or fair with many acts or attractions"
    // Tied to a holiday or season: Halloween, Oktoberfest, Christmas, Día de Muertos.
    EventTag.Holiday -> "A holiday or seasonal celebration such as Halloween, Oktoberfest or Christmas"
    // Vendors selling goods: markets, craft fairs, bazaars.
    EventTag.Market -> "A market, craft fair or vendor fair"
    // Civic and political life: elections, ballot issues, candidate forums, rallies, advocacy and political discussion.
    EventTag.Politics -> "A political event: a rally, candidate forum, ballot discussion, advocacy meeting or civic action"
}
