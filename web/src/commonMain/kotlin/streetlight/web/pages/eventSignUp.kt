package streetlight.web.pages

import koala.FileSet
import koala.Asset
import koala.html.*
import kotlinx.html.*
import streetlight.model.data.*
import koala.css.*

fun HTML.eventSignUp(event: Event, styles: String) {
    appHead("Sign Up | ${event.title}", styles) {
        applyFiles(EventSignUpFiles)
    }
    body {
        column(Id("event-profile"), modify(AlignItemsCenter)) {
            heading1(event.title)
            heading2("Sign Up")

            tabs(Id("signup-tabs")) {
                tab("Guest") {
                    textBlock("hello")
                }
                tab("Sign-in") {
                    textBlock("world")
                }
                tab("Coffee") {
                    textBlock("yes please")
                }
            }
            column(Id("sign-up-box"), modify(Width100Pct)) {
                column(Id("guest-details"), modify(DisplayNone)) {
                    textField(id = Id("name"), label = "Your name")
                    textField(id = Id("email"), label = "Email")
                    btn("Send", invoke("sendRequest"))
                }
                column(Id("user-details"), modify(DisplayNone)) {
                    textBlock("User details form")
                }
            }
        }
    }
}

object EventSignUpFiles: FileSet<Asset>() {
    val css = add("eventSignUp.css")
    val js = add("eventSignUp.js")
}