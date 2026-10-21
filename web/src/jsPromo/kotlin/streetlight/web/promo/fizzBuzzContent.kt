package streetlight.web.promo

/** FizzBuzz Bar & Grill's page as the dev server rendered it: the location, its design, and its events. */
// language="JSON"
internal val fizzBuzzContentJson = """
    {
        "location": {
            "locationId": "eeb9880c-e329-4d7f-b2a8-ebe3047e72af",
            "cityId": "97af74e6-a322-4d2e-8248-383762ca9fe5",
            "timezoneId": "America/Denver",
            "slug": "fizzbuzz-bar-grill-denver",
            "scout": "leadline",
            "host": "Luke",
            "name": "FizzBuzz Bar & Grill",
            "description": "A neighborhood bar on the 16th Street Mall where every third drink is a Fizz, every fifth a Buzz, and every fifteenth is a FizzBuzz on the house. Wood-fired burgers, a rotating tap list of Colorado craft beer, and a back room for live music most nights of the week. Happy hour runs 3 to 6, though the regulars swear it loops forever.",
            "address": "910 16th Street",
            "city": "Denver",
            "state": "Colorado",
            "geoPoint": {
                "lng": -104.9941978,
                "lat": 39.7466116
            },
            "resources": [],
            "tags": [],
            "url": "http://localhost:8080/l/fizzbuzz-bar-grill-denver",
            "starCount": 0,
            "eventCount": 2,
            "isLit": false,
            "eventsUrl": "http://localhost:8080/l/fizzbuzz-bar-grill-denver",
            "image": {
                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp",
                "variants": [
                    {
                        "size": "Thumb",
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-th.webp"
                    },
                    {
                        "size": "Small",
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-sm.webp"
                    },
                    {
                        "size": "Medium",
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-md.webp"
                    },
                    {
                        "size": "Large",
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp"
                    }
                ],
                "aspect": 1.3333334
            },
            "updatedAt": "2026-10-17T18:51:42.073781Z",
            "createdAt": "2026-10-12T16:18:56.895396Z"
        },
        "design": {
            "layout": {
                "blocks": [
                    {
                        "type": "streetlight.model.data.HeaderBlock",
                        "headerType": "Marquee"
                    },
                    {
                        "type": "streetlight.model.data.TabsBlock",
                        "tabs": [
                            {
                                "name": "events",
                                "blocks": [
                                    {
                                        "type": "streetlight.model.data.EventsBlock"
                                    }
                                ]
                            },
                            {
                                "name": "gallery",
                                "blocks": []
                            },
                            {
                                "name": "about us",
                                "blocks": []
                            },
                            {
                                "name": "directions",
                                "blocks": [
                                    {
                                        "type": "streetlight.model.data.MapBlock"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            },
            "theme": {
                "rho": {
                    "color": {
                        "red": 255,
                        "green": 99,
                        "blue": 132
                    },
                    "position": {
                        "x": 33,
                        "y": 36,
                        "radius": 39
                    },
                    "energy": 24,
                    "focus": 37
                },
                "beta": {
                    "color": {
                        "red": 88,
                        "green": 164,
                        "blue": 255
                    },
                    "position": {
                        "x": 71,
                        "y": 37,
                        "radius": 39
                    },
                    "energy": 24,
                    "focus": 48
                },
                "gamma": {
                    "color": {
                        "red": 88,
                        "green": 255,
                        "blue": 188
                    },
                    "position": {
                        "x": 50,
                        "y": 65,
                        "radius": 35
                    },
                    "energy": 10,
                    "focus": 65
                }
            }
        },
        "feed": {
            "entities": [
                {
                    "type": "streetlight.model.data.EventLocation",
                    "eventId": "111f8f14-48c3-475c-868e-3ce42d140c68",
                    "locationId": "eeb9880c-e329-4d7f-b2a8-ebe3047e72af",
                    "eventSlug": "the-binary-blues",
                    "locationSlug": "fizzbuzz-bar-grill-denver",
                    "host": "",
                    "eventImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/6b7941c3-8c8e-4256-9283-7249d457692b-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/6b7941c3-8c8e-4256-9283-7249d457692b-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/6b7941c3-8c8e-4256-9283-7249d457692b-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/6b7941c3-8c8e-4256-9283-7249d457692b-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/6b7941c3-8c8e-4256-9283-7249d457692b-lg.webp"
                            }
                        ],
                        "aspect": 1.5
                    },
                    "title": "The Binary Blues",
                    "description": "The Binary Blues play the only blues in base two: every song has exactly two chords, and both of them are sad. Expect slow-burning ballads about off-by-one errors, a twelve-bar shuffle that compiles on the first try, and the crowd favorite, \"My Baby Left Me (And Took the Semicolons).\" Doors at 7:30, music at 8.",
                    "tags": [
                        "Music",
                        "Concert"
                    ],
                    "cost": 12.8,
                    "status": "Pending",
                    "visibility": 12,
                    "geoPoint": {
                        "lng": -104.9941978,
                        "lat": 39.7466116
                    },
                    "locationName": "FizzBuzz Bar & Grill",
                    "locationDescription": "A neighborhood bar on the 16th Street Mall where every third drink is a Fizz, every fifth a Buzz, and every fifteenth is a FizzBuzz on the house. Wood-fired burgers, a rotating tap list of Colorado craft beer, and a back room for live music most nights of the week. Happy hour runs 3 to 6, though the regulars swear it loops forever.",
                    "address": "910 16th Street",
                    "city": "Denver",
                    "locationImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp"
                            }
                        ],
                        "aspect": 1.3333334
                    },
                    "lightCount": 0,
                    "isLit": false,
                    "startsAt": "2026-10-28T02:00:00Z",
                    "updatedAt": "2026-10-12T16:45:23.471310Z",
                    "createdAt": "2026-10-12T16:25:46.481288Z"
                },
                {
                    "type": "streetlight.model.data.EventLocation",
                    "eventId": "4d2805dc-3333-4b21-8943-ef10cddf2b16",
                    "locationId": "eeb9880c-e329-4d7f-b2a8-ebe3047e72af",
                    "eventSlug": "hello-world-open-mic",
                    "locationSlug": "fizzbuzz-bar-grill-denver",
                    "host": "",
                    "eventImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/2061f267-1457-462c-8c86-c5789856107e-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/2061f267-1457-462c-8c86-c5789856107e-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/2061f267-1457-462c-8c86-c5789856107e-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/2061f267-1457-462c-8c86-c5789856107e-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/2061f267-1457-462c-8c86-c5789856107e-lg.webp"
                            }
                        ],
                        "aspect": 1.3333334
                    },
                    "title": "Hello, World! Open Mic",
                    "description": "Every great program starts with Hello, World, and every great act starts here. Sign up at the bar, grab the mic, and run your code: songs, poems, stand-up, or a dramatic reading of your commit history. Each slot is ten minutes or one infinite loop, whichever ends first. First-timers welcome; there are no bugs here, only undocumented features.",
                    "tags": [
                        "Music",
                        "OpenMic"
                    ],
                    "cost": 0.0,
                    "status": "Pending",
                    "visibility": 10,
                    "geoPoint": {
                        "lng": -104.9941978,
                        "lat": 39.7466116
                    },
                    "locationName": "FizzBuzz Bar & Grill",
                    "locationDescription": "A neighborhood bar on the 16th Street Mall where every third drink is a Fizz, every fifth a Buzz, and every fifteenth is a FizzBuzz on the house. Wood-fired burgers, a rotating tap list of Colorado craft beer, and a back room for live music most nights of the week. Happy hour runs 3 to 6, though the regulars swear it loops forever.",
                    "address": "910 16th Street",
                    "city": "Denver",
                    "locationImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp"
                            }
                        ],
                        "aspect": 1.3333334
                    },
                    "lightCount": 0,
                    "isLit": false,
                    "startsAt": "2026-10-30T01:00:00Z",
                    "updatedAt": "2026-10-12T16:42:59.089707Z",
                    "createdAt": "2026-10-12T16:42:59.089708Z"
                },
                {
                    "type": "streetlight.model.data.EventLocation",
                    "eventId": "6683d672-5ee6-4688-bb78-5fce7fdc8752",
                    "locationId": "eeb9880c-e329-4d7f-b2a8-ebe3047e72af",
                    "eventSlug": "cocktails-and-code",
                    "locationSlug": "fizzbuzz-bar-grill-denver",
                    "host": "",
                    "eventImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/04850d3f-008f-42c8-ae8e-70e6e78a4673-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/04850d3f-008f-42c8-ae8e-70e6e78a4673-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/04850d3f-008f-42c8-ae8e-70e6e78a4673-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/04850d3f-008f-42c8-ae8e-70e6e78a4673-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/04850d3f-008f-42c8-ae8e-70e6e78a4673-lg.webp"
                            }
                        ],
                        "aspect": 1.3317972
                    },
                    "title": "Cocktails and Code",
                    "description": "Bring a laptop, a side project, or just your curiosity. Coders of every level gather in the back room to pair up, swap war stories, and debug over a drink. The bar pours a special menu for the night: the Old Fashioned Loop, the Recursive Mule, and the Segfault Sour, which is served with no warning. Beginners welcome; ask anything, nobody will tell you to read the docs.",
                    "tags": [
                        "Meetup",
                        "Tech",
                        "Networking"
                    ],
                    "cost": 0.0,
                    "status": "Pending",
                    "visibility": 6,
                    "geoPoint": {
                        "lng": -104.9941978,
                        "lat": 39.7466116
                    },
                    "locationName": "FizzBuzz Bar & Grill",
                    "locationDescription": "A neighborhood bar on the 16th Street Mall where every third drink is a Fizz, every fifth a Buzz, and every fifteenth is a FizzBuzz on the house. Wood-fired burgers, a rotating tap list of Colorado craft beer, and a back room for live music most nights of the week. Happy hour runs 3 to 6, though the regulars swear it loops forever.",
                    "address": "910 16th Street",
                    "city": "Denver",
                    "locationImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp"
                            }
                        ],
                        "aspect": 1.3333334
                    },
                    "lightCount": 0,
                    "isLit": false,
                    "startsAt": "2026-10-31T00:00:00Z",
                    "updatedAt": "2026-10-12T17:15:00.417712Z",
                    "createdAt": "2026-10-12T16:48:38.637712Z"
                },
                {
                    "type": "streetlight.model.data.EventLocation",
                    "eventId": "e2d9b136-aaf0-4289-8e23-dfdead1e177c",
                    "locationId": "eeb9880c-e329-4d7f-b2a8-ebe3047e72af",
                    "eventSlug": "null-pointer-costume-party",
                    "locationSlug": "fizzbuzz-bar-grill-denver",
                    "host": "",
                    "eventImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/9f4a916f-c299-41d8-8ac7-19cd76181a14-md.webp",
                        "variants": [
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/9f4a916f-c299-41d8-8ac7-19cd76181a14-md.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/9f4a916f-c299-41d8-8ac7-19cd76181a14-sm.webp"
                            },
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/9f4a916f-c299-41d8-8ac7-19cd76181a14-th.webp"
                            }
                        ],
                        "aspect": 1.3333334,
                        "description": "A crowd dancing under colored lights"
                    },
                    "title": "Null Pointer Costume Party",
                    "description": "Come as your favorite bug. On Halloween night the back room goes dark and DJ Segfault spins until the stack runs out. Best costume wins a FizzBuzz tab that never terminates, and anyone dressed as a null pointer drinks on the house, if the bartender can find you.\n\nFree before 10, ${'$'}10 after.",
                    "tags": [
                        "Music",
                        "DJSet",
                        "Party"
                    ],
                    "cost": 10.0,
                    "status": "Pending",
                    "visibility": 6,
                    "geoPoint": {
                        "lng": -104.9941978,
                        "lat": 39.7466116
                    },
                    "locationName": "FizzBuzz Bar & Grill",
                    "locationDescription": "A neighborhood bar on the 16th Street Mall where every third drink is a Fizz, every fifth a Buzz, and every fifteenth is a FizzBuzz on the house. Wood-fired burgers, a rotating tap list of Colorado craft beer, and a back room for live music most nights of the week. Happy hour runs 3 to 6, though the regulars swear it loops forever.",
                    "address": "910 16th Street",
                    "city": "Denver",
                    "locationImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp"
                            }
                        ],
                        "aspect": 1.3333334
                    },
                    "lightCount": 0,
                    "isLit": false,
                    "startsAt": "2026-11-01T03:00:00Z",
                    "updatedAt": "2026-10-20T21:03:25.049446Z",
                    "createdAt": "2026-10-20T21:03:25.049447Z"
                },
                {
                    "type": "streetlight.model.data.EventLocation",
                    "eventId": "d7e1dd35-8ad9-487e-953c-a2cfc44ffa33",
                    "locationId": "eeb9880c-e329-4d7f-b2a8-ebe3047e72af",
                    "eventSlug": "brute-force-trivia-night",
                    "locationSlug": "fizzbuzz-bar-grill-denver",
                    "host": "",
                    "eventImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/454ebff4-cbf9-4723-87f5-6f9baf2998df-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/454ebff4-cbf9-4723-87f5-6f9baf2998df-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/454ebff4-cbf9-4723-87f5-6f9baf2998df-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/454ebff4-cbf9-4723-87f5-6f9baf2998df-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/454ebff4-cbf9-4723-87f5-6f9baf2998df-lg.webp"
                            }
                        ],
                        "aspect": 1.3333334
                    },
                    "title": "Brute Force Trivia Night",
                    "description": "Six rounds, every answer worth a try. Pub classics mixed with a round of tech trivia, a music round played on the house speakers, and a final question worth double points. Teams of up to six; the winners take home a bar tab and eternal bragging rights in the commit log behind the bar. Guessing every possible answer is allowed, but you only get one sheet.",
                    "tags": [
                        "GamesAndTrivia",
                        "FoodAndDrink"
                    ],
                    "cost": 0.0,
                    "status": "Pending",
                    "visibility": 13,
                    "geoPoint": {
                        "lng": -104.9941978,
                        "lat": 39.7466116
                    },
                    "locationName": "FizzBuzz Bar & Grill",
                    "locationDescription": "A neighborhood bar on the 16th Street Mall where every third drink is a Fizz, every fifth a Buzz, and every fifteenth is a FizzBuzz on the house. Wood-fired burgers, a rotating tap list of Colorado craft beer, and a back room for live music most nights of the week. Happy hour runs 3 to 6, though the regulars swear it loops forever.",
                    "address": "910 16th Street",
                    "city": "Denver",
                    "locationImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp"
                            }
                        ],
                        "aspect": 1.3333334
                    },
                    "lightCount": 0,
                    "isLit": false,
                    "startsAt": "2026-11-05T02:30:00Z",
                    "updatedAt": "2026-10-12T16:48:47.911922Z",
                    "createdAt": "2026-10-12T16:48:47.911923Z"
                },
                {
                    "type": "streetlight.model.data.EventLocation",
                    "eventId": "4a2e0f76-1da3-425d-abc3-197c15642fea",
                    "locationId": "eeb9880c-e329-4d7f-b2a8-ebe3047e72af",
                    "eventSlug": "stack-overflow-and-the-except",
                    "locationSlug": "fizzbuzz-bar-grill-denver",
                    "host": "",
                    "eventImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/43d7ea17-0753-4986-8d2c-43886e86d1fc-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/43d7ea17-0753-4986-8d2c-43886e86d1fc-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/43d7ea17-0753-4986-8d2c-43886e86d1fc-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/43d7ea17-0753-4986-8d2c-43886e86d1fc-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/43d7ea17-0753-4986-8d2c-43886e86d1fc-lg.webp"
                            }
                        ],
                        "aspect": 1.3333334
                    },
                    "title": "Stack Overflow and the Exceptions",
                    "description": "Denver's loudest garage rock band brings its unhandled energy to the back room. Expect fast riffs, faster drums, and a setlist that throws something new every night. Their latest single, \"Works on My Machine,\" has been stuck in heads all over the city. Opening act to be determined at runtime. Doors at 8, earplugs at the bar.",
                    "tags": [
                        "Music",
                        "Concert"
                    ],
                    "cost": 15.0,
                    "status": "Pending",
                    "visibility": 16,
                    "geoPoint": {
                        "lng": -104.9941978,
                        "lat": 39.7466116
                    },
                    "locationName": "FizzBuzz Bar & Grill",
                    "locationDescription": "A neighborhood bar on the 16th Street Mall where every third drink is a Fizz, every fifth a Buzz, and every fifteenth is a FizzBuzz on the house. Wood-fired burgers, a rotating tap list of Colorado craft beer, and a back room for live music most nights of the week. Happy hour runs 3 to 6, though the regulars swear it loops forever.",
                    "address": "910 16th Street",
                    "city": "Denver",
                    "locationImage": {
                        "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp",
                        "variants": [
                            {
                                "size": "Thumb",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-th.webp"
                            },
                            {
                                "size": "Small",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-sm.webp"
                            },
                            {
                                "size": "Medium",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-md.webp"
                            },
                            {
                                "size": "Large",
                                "url": "https://fast-shoal.s3.us-east-va.io.cloud.ovh.us/daa7001d-95d5-4488-a378-e1e58a2761b5-lg.webp"
                            }
                        ],
                        "aspect": 1.3333334
                    },
                    "lightCount": 0,
                    "isLit": false,
                    "startsAt": "2026-11-08T04:00:00Z",
                    "updatedAt": "2026-10-12T16:53:30.608984Z",
                    "createdAt": "2026-10-12T16:53:30.608985Z"
                }
            ],
            "source": {
                "context": {
                    "type": "streetlight.model.data.FeedContext.Location",
                    "locationId": "eeb9880c-e329-4d7f-b2a8-ebe3047e72af"
                },
                "type": "Events"
            }
        },
        "canEdit": false
    }
"""
