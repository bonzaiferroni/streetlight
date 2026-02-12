package streetlight.web.pages

import koala.html.*
import kotlinx.html.*
import streetlight.model.data.*
import koala.css.*

fun HTML.eventSignUp(event: Event) {
    head("Sign Up | ${event.title}") {
        styles("eventSignUp.css")
        scripts("eventSignUp.js")
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
            column(Id("sign-up-box"), modify(Width100)) {
                column(Id("guest-details"), modify(DisplayNone)) {
                    textField(Id("name"), "Your name")
                    textField(Id("email"), "Email")
                    button("Send", invoke("sendRequest"))
                }
                column(Id("user-details"), modify(DisplayNone)) {
                    textBlock("User details form")
                }
            }
        }
    }
}