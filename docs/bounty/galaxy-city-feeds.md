# Galaxy and City Feeds

Turn the galaxy and city lists into feeds on `FeedContext`, with search, and rename their views `viewGalaxyFeed` and `viewCityFeed`.

## Shape

| Part | Galaxies | Cities |
|---|---|---|
| `FeedContext` | `Galaxies` | `Cities` |
| `FeedType` | `StarGalaxies` (signed in only), `TopGalaxies`, `NewGalaxies` | `Cities` |
| Order | The star's own by name; top by star count; new by creation, newest first | By name |
| Cursor | `Name`, `Score`, `Time` | `Name` |
| Search | Name and tagline | Name |
| Route | `GalaxyFeedRoute(feed)`, path `galaxies` | `CityFeedRoute(feed)`, path `cities` |
| Content | `GalaxyFeedContent` | `CityFeedContent` |
| Shell | `galaxyFeedShell`: `pageHeader` and `feedSection` | `cityFeedShell`: `pageHeader` with the city placeholder image, and `feedSection` |
| View | `viewGalaxyFeed`, following the route's feed | `viewCityFeed` |

## Steps

1. 🍻 `model`: the contexts, types, `EntityCursor.Name`, routes, screens, content and endpoints.
2. 🍻 `server`: the paged reads with search, the feed service, content and page rendering.
3. 🍻 `web`: shells, views, the search field for the new types, and the places that name the old routes.
4. 🍻 Tests: `GalaxiesFeedTest` and `CitiesFeedTest`, paging, order, search and what a visitor is offered.
5. 🍻 Compile, run the tests, amend the documents.

## Decisions

| Decision | Reason |
|---|---|
| Screens, routes and content are renamed with the views, as `GalaxyFeed` and `CityFeed` | One name per screen; the paths stay `galaxies` and `cities` |
| `EntityCursor.Name` pages by name | No cursor sorts by text |
| A feed read takes the search of any cursor, and pages with its own kind | The section's refresh sends an upcoming cursor whatever the feed |
| Post feeds read a `Name` cursor as their first page, newest first | Posts are never sorted by name, and the cursor's `when` must cover it |
| Icons: `Star` for a star's galaxies, `Trophy` for top, `Seedling` for new, `City` for cities | |
| The city feed's descriptor is "Streetlight Cities" | Parallel to the galaxy feed's "Streetlight Communities" |

## Questions

## Known Failure

`CityFeedTest` > `a city map holds its locations and only upcoming events` fails, apart from this bounty: `cityEntityQuery` shows a location only when it has no upcoming events, while the test expects both the location and its event.

## Scratch Points
