# Home

* [UI map](../ui-map.md)

## Purpose
Gives visitors an overview of the site — what it is, what's happening, and where to go next.

## Resources

| Resource | Definition               | File                                                                                       |
|----------|--------------------------|--------------------------------------------------------------------------------------------|
| Route    | `HomeShell`              | [Screen.kt](../../../model/src/commonMain/kotlin/streetlight/model/ui/Screen.kt) |
| View     | `viewHome()`             | [viewHome.kt](../../../web/src/jsMain/kotlin/streetlight/web/view/viewHome.kt)                  |
| Shell    | `homeShell(HomeContent)` | [homeShell.kt](../../../web/src/commonMain/kotlin/streetlight/web/shell/homeShell.kt)        |
| Model    | -                        |                                                                                            |

* [HomeContent](../../../web/src/commonMain/kotlin/streetlight/web/shell/homeShell.kt)
  * galaxies: `List<Galaxy>`
  * posts: `List<Post>`

## Sections

* Map window
* Navigation
  * Galaxy menu
* Top galaxies, featured galaxies
* Top posts
* User events