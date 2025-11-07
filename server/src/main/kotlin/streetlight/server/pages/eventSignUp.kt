package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*
import streetlight.model.data.*

fun HTML.eventSignUp(event: Event) {
    head("Sign Up | ${event.title}") {
        coreStyles()
        coreScripts()
        styles("eventSignUp.css")
        scripts("eventSignUp.js")
    }
    body {
        column(Id("event-profile"), AlignItemsCenter) {
            heading1(event.title)
            heading2("Sign Up")

            tabs {
                tab("Guest") {
                    paragraph("hello")
                }
                tab("Sign-in") {
                    paragraph("world")
                }
            }
            column(Id("sign-up-box"), FillWidth) {
                column(Id("guest-details"), DisplayNone) {
                    textField(Id("name"), "Your name")
                    textField(Id("email"), "Email")
                    button("Send", invoke("sendRequest"))
                }
                column(Id("user-details"), DisplayNone) {
                    paragraph("User details form")
                }
            }
        }
    }
}