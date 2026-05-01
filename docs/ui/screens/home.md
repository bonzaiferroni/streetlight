# Home

* [UI map](../ui-map.md)

## Purpose
Gives visitors an overview of the site — what it is, what's happening, and where to go next.

## Resources

| Resource | Definition               | File                                                                                       |
|----------|--------------------------|--------------------------------------------------------------------------------------------|
| Route    | `HomeShell`              | [StreetlightRoute.kt](../../../web/src/commonMain/kotlin/streetlight/web/StreetlightRoute.kt) |
| View     | `viewHome()`             | [viewHome.kt](../../../web/src/jsMain/kotlin/streetlight/web/ui/viewHome.kt)                  |
| Shell    | `homeShell(HomeContent)` | [homeShell.kt](../../../web/src/commonMain/kotlin/streetlight/web/shells/homeShell.kt)        |
| Model    | -                        |                                                                                            |

* [HomeContent](../../../web/src/commonMain/kotlin/streetlight/web/shells/homeShell.kt)
  * galaxies: `List<Galaxy>`
  * posts: `List<Post>`

## Sections

* Map window
* Navigation
  * Galaxy menu
* Top galaxies, featured galaxies
* Top posts
* User events