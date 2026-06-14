package streetlight.web.ui

import kampfire.api.toSlug
import kampfire.model.thumb
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.button
import koala.html.heading3
import koala.html.icon
import koala.html.image
import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.onClick
import org.w3c.dom.HTMLElement
import streetlight.model.data.Star
import streetlight.web.SiteConfigRoute
import streetlight.web.StarDashRoute
import streetlight.web.StarRoute
import streetlight.web.model.CredentialStore
import streetlight.web.model.UserGate
import streetlight.web.pages.HelmBarKey
import streetlight.web.pages.StarHelmKey

fun AppScope.queryAndWireStarHelm() {
    val element = document.body?.querySelector(StarHelmKey.ContentId) ?: error("star helm content not found")
    wireStarHelm(element)
}

private fun AppScope.wireStarHelm(element: HTMLElement) {
    val gate = app.get<UserGate>()

    wireBlock(element) {
        flowBlock(gate.starFlow, defaultMagic) { star ->
            if (star != null) {
                starPanel(star)
            } else {
                someonePanel()
            }
        }
    }
}

private val RowMod = modify(AlignItemsCenter, PaddingLeft3, JustifyContentEnd)

private fun AppScope.starPanel(star: Star) {
    val gate = app.get<UserGate>()

    column() {
        row(RowMod) {
            navigation(StarRoute(star.username)) {
                heading3(star.username.value)
            }

            button(modify(HelmBarKey.IconMod, FadeLoop)) {
                onClick = StarHelmKey.ClosePopover

                image(star.images.thumb, modify(OpacityHigh, Size100P, BorderRadius50P))
            }
        }

        // calendar route goes here

        navigation(StarDashRoute) { // filler content
            onClick = StarHelmKey.ClosePopover

            row(RowMod) {
                textBlock("Dashboard")
                icon(SvgFile.Dashboard, HelmBarKey.IconMod)
            }
        }

        button(onClick = gate::signOut) {
            row(RowMod) {
                textBlock("Sign out", modify(WhiteSpaceNoWrap))
                icon(SvgFile.SignOut, HelmBarKey.IconMod)
            }
        }
    }
}

private fun AppScope.someonePanel() {
    column(modify(OverflowClip, MinWidth32)) {
        row(RowMod) {
            heading3("Someone")
            button(modify(HelmBarKey.IconMod, FadeLoop)) {
                onClick = StarHelmKey.ClosePopover

                image(SvgFile.Someone, modify(OpacityHigh, Size100P, BorderRadius50P))
            }
        }

        tabs(Id("someone-tabs")) {
            tab("Sign in") {
                gateForm()
            }
            tab("Alternative") {
                // calendar route goes here
                navigation(SiteConfigRoute) { // filler content
                    onClick = StarHelmKey.ClosePopover

                    row(RowMod + Width32) {
                        textBlock("Using Streetlight while signed-out", modify(TextAlignRight))
                        icon(SvgFile.Info, HelmBarKey.IconMod)
                    }
                }

                navigation(SiteConfigRoute) { // filler content
                    onClick = StarHelmKey.ClosePopover

                    row(RowMod) {
                        textBlock("Settings")
                        icon(SvgFile.Settings, HelmBarKey.IconMod)
                    }
                }
            }
        }
    }
}

fun AppScope.gateForm() {
    val gate = app.get<UserGate>()
    val cred = app.get<CredentialStore>()

    column {
        textField(
            label = "username/email",
            onValue = cred::setUsername,
            placeholder = "username/email",
            flow = cred.usernameFlow
        ) {
            autoComplete = "username"
        }
        textField(
            label = "password",
            onValue = cred::setPassword,
            placeholder = "password",
            flow = cred.passwordFlow
        ) {
            type = InputType.password
            autoComplete = "password"
        }
        checkBox("Stay signed in", cred::setStayLoggedIn, cred.stayLoggedInFlow)
        row {
            button("sign in", modifiers = modify(Accent), onClickEvent = {
                gate.signIn()
            })
        }
    }
}

