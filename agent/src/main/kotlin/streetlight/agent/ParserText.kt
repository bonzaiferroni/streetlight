package streetlight.agent

//language="MD"
object ParserText {
    val locationProperties = """
        * name: name of the location, should be normal casing and appropriately capitalized
        * description: description of the location
        * address: house or building number and street name of the location
        * postalCode: Postal code of the location
        * state: State of the location, two letter abbreviation if relevant (e.g. CO)
        * country: Country of the location
        * url: home page for the location
        * eventsUrl: web address for more information about upcoming events at the location, like a calendar
        * aboutUrl: web address for more information about the location
        * menuUrl: web address for a list of food/drink items available at the location
        * imageUrl: Featured image of the location. If the source is the home page for the location, look for a meta tag with og:image or twitter:image properties.
    """.trimIndent()

    val eventProperties = """
        * name: Event name or title, should be normal casing and appropriately capitalized
        * startTime: Time of day of the event as 24-hour value [HH:MM]
        * endTime: Time when the event ends as 24-hour value [HH:MM]. Must be different from startTime. 
        * date: Date of the event as ISO local date [YYYY-MM-DD]
        * location: The name or description of the location of the event
        * address: The address at which the event is located
        * imageUrl: The featured image for the event, must be a full url
        * description: Additional details given about the event. Provide rich formatting with markdown. Preserve paragraphs.
        * ageMin: The minimum age for attendees
        * cost: The lowest entry fee or ticket price for the event in USD. Provide the string value Free if there is no cost.
        * contact: Any name and/or contact information given for the event
        * url: Url for more information about the event, must be a full url
        
        Only provide details for the first 20 events listed.
    """.trimIndent()

    val locationInstructions = """
        Read the html above. We believe it is information about a venue or location that hosts events.
        
        Try to determine the following:
        $locationProperties
    """.trimIndent()

    val coldInstructions = """
        Read the html above. We believe it is information about an event or a list of events. It might also
        have information about an event location. You will parse information about the events and their location.
              
        For the location, determine the following:
        $locationProperties
        
        For each event, determine the following:
        $eventProperties
""".trimIndent()

    val multiEventInstructions = """
        Read the html above. We believe it is information about an event or a list of events. 
        
        For each event, determine the following:
        $eventProperties
    """.trimIndent()

    val singleEventInstructions = """
        Read the html above. We believe it is information about an event.
        
        Determine the following:
        $eventProperties
    """.trimIndent()

}

// @Serializable
//data class EventSchema(
//    val feed: EventFeedSelectors,
//    val page: EventPageSelectors?,
//)
//
//@Serializable
//data class EventFeedSelectors(
//    val event: String?,
//    val title: String?,
//    val link: String?,
//    val cost: String?,
//    val description: String?,
//    val time: String?,
//)