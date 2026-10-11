package streetlight.server.daemon.agent

import streetlight.model.data.LocationTag

/**
 * A short description of this tag, the guide to annotating locations with it.
 *
 * A location takes every tag that names what it is or what people come to it for, as its own page or the map
 * presents it. A narrow tag implies a broad one only where its rule says "Also".
 */
fun LocationTag.getClassifierLabel(): String = when (this) {
    // Food & Drink. Bar is broad: any place whose main business is serving drinks on site, except a producer. A
    // producer (Brewery, Winery, Distillery, Cidery) takes its own tag alone, its tasting room included. Restaurant is
    // broad: any sit-down place whose main business is meals.
    LocationTag.Restaurant -> "A restaurant serving sit-down meals"
    LocationTag.Cafe -> "A cafe or coffee shop serving coffee, pastries and light meals"
    LocationTag.Bar -> "A bar serving drinks, such as a tavern, saloon or neighborhood bar"
    // Calls itself a pub or public house, often Irish or British in style. Also Bar.
    LocationTag.Pub -> "A pub or public house, often Irish or British in style"
    // Brews its own beer on site.
    LocationTag.Brewery -> "A brewery or brewpub that brews its own beer, with a taproom"
    // Pours beer on tap without brewing it there, such as a craft beer bar or a brewery's satellite taproom. Also Bar,
    // unless a producer.
    LocationTag.Taproom -> "A taproom or craft beer bar pouring many beers on tap"
    LocationTag.Winery -> "A winery or vineyard that makes its own wine, with a tasting room"
    // Also Bar.
    LocationTag.WineBar -> "A wine bar serving wine by the glass"
    LocationTag.Distillery -> "A distillery making spirits such as whiskey, gin or vodka, with a tasting room"
    LocationTag.Cidery -> "A cidery making hard cider or mead, with a tasting room"
    // Also Bar, unless a producer.
    LocationTag.BeerGarden -> "An outdoor beer garden with long tables"
    // Also Bar.
    LocationTag.CocktailBar -> "A cocktail bar serving craft cocktails"
    // Also Bar and CocktailBar.
    LocationTag.Speakeasy -> "A hidden speakeasy-style cocktail bar"
    // Also Bar.
    LocationTag.SportsBar -> "A sports bar showing games on many screens"
    // Also Bar.
    LocationTag.Lounge -> "A lounge with seating for drinks and conversation, such as a hookah or cigar lounge"
    LocationTag.FoodHall -> "A food hall or food court with many vendors under one roof"
    LocationTag.FoodTruck -> "A food truck or mobile kitchen"
    LocationTag.Bakery -> "A bakery selling bread, pastries and baked goods"
    LocationTag.IceCream -> "An ice cream, gelato or frozen yogurt shop"
    LocationTag.DessertShop -> "A dessert shop selling sweets, cupcakes, donuts or chocolate"
    // Also Restaurant when it seats diners.
    LocationTag.Pizzeria -> "A pizzeria or pizza restaurant"
    LocationTag.TeaHouse -> "A tea house or tea room"
    // Also Restaurant.
    LocationTag.Diner -> "A diner serving breakfast and classic American comfort food"
    // Counter service from a chain or quick kitchen. Not Restaurant.
    LocationTag.FastFood -> "A fast food restaurant with counter service"
    LocationTag.Deli -> "A deli or sandwich shop"
    // Also Restaurant.
    LocationTag.Barbecue -> "A barbecue or smokehouse restaurant"
    // Also Restaurant.
    LocationTag.Bistro -> "A bistro or small restaurant with a chef-driven menu"

    // Entertainment
    // A stage for plays, musicals, dance or opera.
    LocationTag.Theater -> "A theater staging plays, musicals and performances"
    // Also Theater.
    LocationTag.OperaHouse -> "An opera house"
    LocationTag.Cinema -> "A movie theater or cinema"
    LocationTag.DriveIn -> "A drive-in movie theater"
    // Presents itself as a place to hear live music, by name, stage or calendar of bands. A bar that only has music
    // some nights is not.
    LocationTag.MusicVenue -> "A live music venue or club hosting bands and concerts"
    // A hall built for orchestras and concerts. Also MusicVenue.
    LocationTag.ConcertHall -> "A concert hall for orchestras and concerts"
    // Also MusicVenue when it hosts concerts.
    LocationTag.Amphitheater -> "An outdoor amphitheater or open-air stage"
    LocationTag.Nightclub -> "A nightclub with DJs and dancing late at night"
    // Also MusicVenue.
    LocationTag.JazzClub -> "A jazz club"
    LocationTag.ComedyClub -> "A comedy club for stand-up and improv"
    // Also Bar.
    LocationTag.KaraokeBar -> "A karaoke bar"
    // A space rented for weddings, parties and events.
    LocationTag.EventVenue -> "An event venue rented for weddings, parties and gatherings"
    LocationTag.BanquetHall -> "A banquet or ballroom hall for receptions"
    LocationTag.ConferenceCenter -> "A conference or convention center"
    LocationTag.DanceStudio -> "A dance studio or ballroom offering dance classes"
    LocationTag.Arcade -> "An arcade with video and pinball games"
    LocationTag.GamingLounge -> "A gaming lounge or board game cafe"
    LocationTag.BowlingAlley -> "A bowling alley"
    LocationTag.Billiards -> "A billiards or pool hall"
    LocationTag.Casino -> "A casino"
    LocationTag.EscapeRoom -> "An escape room"
    LocationTag.LaserTag -> "A laser tag or paintball arena"
    LocationTag.GoKartTrack -> "A go-kart track"
    LocationTag.MiniGolf -> "A mini golf course"
    LocationTag.TrampolinePark -> "A trampoline park"
    LocationTag.Circus -> "A circus"
    LocationTag.AmusementPark -> "An amusement or theme park with rides"
    LocationTag.WaterPark -> "A water park"

    // Arts & Culture
    LocationTag.Museum -> "A museum of art, history or science"
    // Also Museum.
    LocationTag.ChildrensMuseum -> "A children's museum"
    LocationTag.Gallery -> "An art gallery"
    // Hosts several arts under one roof: classes, galleries and performances.
    LocationTag.ArtsCenter -> "An arts center with galleries, classes and performances"
    LocationTag.ArtStudio -> "An art studio for making art or taking art classes"
    LocationTag.PotteryStudio -> "A pottery or ceramics studio"
    LocationTag.PublicArt -> "A public artwork, mural or sculpture"
    LocationTag.Library -> "A public library"
    LocationTag.Bookstore -> "A bookstore"
    LocationTag.RecordStore -> "A record store selling vinyl"
    LocationTag.CulturalCenter -> "A cultural center for a heritage or community"
    LocationTag.HistoricSite -> "A historic site, house or landmark"
    LocationTag.Monument -> "A monument"
    LocationTag.Memorial -> "A memorial"
    LocationTag.Lighthouse -> "A lighthouse"
    LocationTag.Zoo -> "A zoo or wildlife park"
    LocationTag.Aquarium -> "An aquarium"
    LocationTag.Planetarium -> "A planetarium"
    LocationTag.Observatory -> "An observatory"
    // Also Museum.
    LocationTag.ScienceCenter -> "A science center with hands-on exhibits"
    // A place visitors come to see that no narrower tag names.
    LocationTag.Attraction -> "A tourist attraction"
    LocationTag.Viewpoint -> "A scenic viewpoint or overlook"

    // Community. PlaceOfWorship is broad: Church, Mosque, Synagogue and Temple are also PlaceOfWorship.
    LocationTag.CommunityCenter -> "A community center hosting local programs and gatherings"
    LocationTag.PlaceOfWorship -> "A place of worship for a faith community"
    LocationTag.Church -> "A church"
    LocationTag.Mosque -> "A mosque"
    LocationTag.Synagogue -> "A synagogue"
    LocationTag.Temple -> "A temple, such as a Buddhist or Hindu temple"
    LocationTag.SeniorCenter -> "A senior center"
    LocationTag.YouthCenter -> "A youth center or teen center"
    // A city-run center with gyms, pools and classes.
    LocationTag.RecreationCenter -> "A recreation center with gyms, pools and classes"
    LocationTag.CityHall -> "A city hall or town hall"
    LocationTag.Courthouse -> "A courthouse"
    LocationTag.CivicCenter -> "A civic center or government building"
    LocationTag.FireStation -> "A fire station"
    LocationTag.Coworking -> "A coworking space"
    LocationTag.Makerspace -> "A makerspace or hackerspace with shared tools"
    LocationTag.SocialClub -> "A members' social club"
    LocationTag.FraternalLodge -> "A fraternal lodge, such as the Elks, Moose or Masons"
    LocationTag.VeteransHall -> "A veterans hall, such as a VFW or American Legion post"
    LocationTag.Nonprofit -> "The office of a nonprofit organization"
    LocationTag.AnimalShelter -> "An animal shelter or rescue"
    LocationTag.FoodBank -> "A food bank or food pantry"
    LocationTag.Hospital -> "A hospital"
    LocationTag.Clinic -> "A medical clinic or health center"

    // Education
    LocationTag.Preschool -> "A preschool or daycare"
    LocationTag.School -> "An elementary, middle or high school"
    LocationTag.College -> "A college or community college"
    LocationTag.University -> "A university campus or building"
    LocationTag.TrainingCenter -> "A training center offering professional courses"
    LocationTag.LanguageSchool -> "A language school"
    LocationTag.MusicSchool -> "A music school offering lessons"
    LocationTag.CookingSchool -> "A cooking school"

    // Outdoors. Park is broad: a public park, and also a DogPark, Playground or SkatePark that is a park of its own.
    LocationTag.Park -> "A public park with lawns, paths and trees"
    LocationTag.Garden -> "A garden"
    LocationTag.BotanicalGarden -> "A botanical garden"
    LocationTag.CommunityGarden -> "A community garden"
    LocationTag.NatureReserve -> "A nature reserve, open space or wildlife refuge"
    LocationTag.NatureCenter -> "A nature center with exhibits and trails"
    LocationTag.Campground -> "A campground"
    LocationTag.Trailhead -> "A trailhead or hiking trail"
    LocationTag.Plaza -> "A public plaza or square"
    LocationTag.Beach -> "A beach"
    LocationTag.Lake -> "A lake or reservoir"
    LocationTag.River -> "A river or creek"
    LocationTag.Marina -> "A marina"
    LocationTag.Pier -> "A pier"
    LocationTag.DogPark -> "A dog park"
    LocationTag.Playground -> "A playground"
    LocationTag.PicnicArea -> "A picnic area or pavilion"
    LocationTag.Mountain -> "A mountain or peak"
    LocationTag.SkatePark -> "A skate park"
    LocationTag.BikePath -> "A bike path or greenway"
    LocationTag.Farm -> "A farm"
    LocationTag.Ranch -> "A ranch"
    LocationTag.Orchard -> "An orchard or pick-your-own farm"
    LocationTag.Cemetery -> "A cemetery"
    LocationTag.Fairgrounds -> "A fairgrounds"

    // Sports & Fitness
    LocationTag.Stadium -> "A stadium for sports and big concerts"
    LocationTag.Arena -> "An indoor arena for sports and concerts"
    // A facility for playing several sports, indoors or out.
    LocationTag.SportsCenter -> "A sports center with courts and fields for many sports"
    LocationTag.Gym -> "A gym or fitness center"
    LocationTag.YogaStudio -> "A yoga or pilates studio"
    LocationTag.MartialArtsStudio -> "A martial arts studio or dojo"
    LocationTag.BoxingGym -> "A boxing gym"
    LocationTag.ClimbingGym -> "A climbing or bouldering gym"
    LocationTag.SwimmingPool -> "A swimming pool"
    LocationTag.SportsField -> "A sports field"
    LocationTag.BaseballField -> "A baseball or softball field"
    LocationTag.SoccerField -> "A soccer field"
    LocationTag.BasketballCourt -> "A basketball court"
    LocationTag.TennisCourt -> "A tennis court"
    LocationTag.PickleballCourt -> "A pickleball court"
    LocationTag.VolleyballCourt -> "A volleyball court"
    LocationTag.GolfCourse -> "A golf course or club"
    LocationTag.DiscGolfCourse -> "A disc golf course"
    LocationTag.RunningTrack -> "A running track"
    LocationTag.IceRink -> "An ice rink"
    LocationTag.RollerRink -> "A roller skating rink"
    LocationTag.SkiResort -> "A ski resort"
    LocationTag.Racetrack -> "A racetrack for cars or horses"
    LocationTag.EquestrianCenter -> "An equestrian center or riding stable"
    LocationTag.Boathouse -> "A boathouse renting kayaks, canoes or paddleboards"

    // Shopping is broad: any place people go to shop, such as a store, mall or market. Every tag of this group, and a
    // Bookstore or RecordStore, is also Shopping.
    LocationTag.Market -> "A public market or marketplace"
    LocationTag.FarmersMarket -> "A farmers market"
    LocationTag.FleaMarket -> "A flea market or swap meet"
    LocationTag.Mall -> "A shopping mall or shopping center"
    LocationTag.Shopping -> "A place to shop, such as a store, mall or market"
    LocationTag.Boutique -> "A clothing boutique"
    LocationTag.GroceryStore -> "A grocery store or supermarket"
    LocationTag.GameStore -> "A game store selling board, card or tabletop games"
    LocationTag.ComicShop -> "A comic book shop"
    LocationTag.HobbyShop -> "A hobby shop"
    LocationTag.CraftStore -> "A craft or yarn store"
    LocationTag.MusicStore -> "A music store selling instruments"
    LocationTag.ThriftStore -> "A thrift or secondhand store"
    LocationTag.AntiqueShop -> "An antique shop"
    LocationTag.GardenCenter -> "A garden center or plant nursery"
    LocationTag.BikeShop -> "A bike shop"
    LocationTag.OutdoorStore -> "An outdoor gear store"
    LocationTag.WineShop -> "A wine or liquor store"
    LocationTag.PetStore -> "A pet store"

    // Health & Wellness
    LocationTag.Spa -> "A spa offering massage and treatments"
    LocationTag.WellnessCenter -> "A wellness center offering holistic health services"
    LocationTag.MeditationCenter -> "A meditation center"
    LocationTag.HotSprings -> "Hot springs"
    LocationTag.Salon -> "A hair or beauty salon"
    LocationTag.BarberShop -> "A barber shop"
    LocationTag.TattooParlor -> "A tattoo parlor"

    // Lodging
    LocationTag.Hotel -> "A hotel"
    LocationTag.Resort -> "A resort"
    LocationTag.Hostel -> "A hostel"
    LocationTag.BedAndBreakfast -> "A bed and breakfast or inn"
    LocationTag.RetreatCenter -> "A retreat center"
    LocationTag.Cabin -> "A cabin or lodge"

    // Transit & Other
    LocationTag.TrainStation -> "A train or transit station"
    LocationTag.Airport -> "An airport"
    LocationTag.Boat -> "A boat or ship"
    LocationTag.Bridge -> "A bridge"
    LocationTag.ParkingLot -> "A parking lot or garage"
    LocationTag.Street -> "A street, block or corner"
    LocationTag.Residence -> "A private residence"
    LocationTag.Office -> "An office or business building"
    LocationTag.Warehouse -> "A warehouse or industrial space"
    LocationTag.Rooftop -> "A rooftop space"
}
