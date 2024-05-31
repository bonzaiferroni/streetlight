package streetlight.server.html

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title

fun HTML.homePage() {
    pageHeader("Home")
    body {
        h1 {
            +"Hello world!"
        }
    }
}