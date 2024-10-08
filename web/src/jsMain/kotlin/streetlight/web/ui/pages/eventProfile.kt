package streetlight.web.ui.pages

import io.kvision.core.Container
import io.kvision.core.JustifyContent
import io.kvision.core.onClickLaunch
import io.kvision.form.check.radio
import io.kvision.form.check.radioGroup
import io.kvision.form.form
import io.kvision.form.text.text
import io.kvision.html.*
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.state.bind
import io.kvision.utils.perc
import streetlight.web.Layout
import streetlight.web.core.PortalEvents
import streetlight.web.description
import streetlight.web.gap
import streetlight.web.subscribe
import streetlight.web.ui.components.card
import streetlight.web.ui.components.typography
import streetlight.web.ui.models.EventProfileModel

fun Container.eventProfile(id: Int): PortalEvents? {
    val model = EventProfileModel(id)
    typography() {
        gap = Layout.defaultGap
        hPanel() {
            gap = Layout.defaultGap
            link("back", "#/event/${id - 1}")
            link("next", "#/event/${id + 1}")
        }
        h1(className = "text-center").bind(model.eventStream) {
            this.content = it.user.username + " @ " + it.location.name
        }
        image("img/bridge.jpg") {
            width = 100.perc
        }
        p().bind(model.eventStream) {
            this.content = it.event.description ?: description
        }
        card {
            p {
                span("now playing: ")
                span().bind(model.eventStream) {
                    this.content = it.currentRequest?.songName
                }
            }
            p {
                span("next up: ")
                span().bind(model.eventStream) {
                    this.content = it.requests.joinToString(", ") { r -> r.songName }
                }
            }
        }
        h2("Request a song")
        form {
            hPanel(justify = JustifyContent.SPACEEVENLY) {
                gap = Layout.defaultGap
                vPanel() {
                    gap = Layout.defaultGap
                    width = 50.perc
                    radioGroup(label = "Options") {
                        radio(true, label = "Luke sings")
                        radio(false, label = "Duet")
                        radio(false, label = "I'll sing solo")
                    }
                }
                vPanel() {
                    gap = Layout.defaultGap
                    width = 50.perc
                    text() {
                        placeholder = "Your name (optional)"
                    }
                    text() {
                        placeholder = "Other notes (optional)"
                    }
                }
            }
        }

        model.songStream.subscribe { songs ->
            songs.forEach { song ->
                card {
                    hPanel(justify = JustifyContent.SPACEBETWEEN) {
                        vPanel {
                            p(song.name)
                            p(song.artist)
                        }
                        button("request") {
                            onClickLaunch {
                                model.makeRequest(song)
                            }
                        }.bind(model.eventStream) {
                            val requested = it.requests.any { r -> r.songId == song.id }
                            this.text = if (requested) "requested" else "request"
                            this.disabled = requested
                        }
                    }
                }
            }
        }
    }
    return null
}

