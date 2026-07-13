package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.button
import koala.html.heading3
import koala.html.icon
import koala.html.image
import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.onClick
import org.w3c.dom.HTMLElement
import streetlight.model.data.Star
import streetlight.model.ui.StarDashRoute
import streetlight.model.ui.StarRoute
import streetlight.web.model.CredentialStore
import streetlight.web.model.StarSession
import streetlight.web.pages.HelmBar
import streetlight.web.pages.StarHelm

fun AppScope.queryAndWireStarHelm() {
    val helmElement = document.body?.querySelector(StarHelm.HelmMenu) ?: error("star helm content not found")
    wireStarMenu(helmElement)
    val barElement = document.body?.querySelector(StarHelm.BarMenu) ?: error("star bar element not found")
    wireStarMenu(barElement)
}

fun AppScope.wireStarMenu(element: HTMLElement) {
    wireBlock(element) {
        starGate(
            openInitially = false,
            baseContent = {
                someonePanel(it)
            }
        ) { star ->
            starPanel(star)
        }
    }
}

private val RowMod = modify(AlignItemsCenter, PaddingLeft3, JustifyContentEnd)

private fun AppScope.starPanel(star: Star) {
    val session = app.get<StarSession>()

    column(modify(AlignItemsEnd)) {
        row(RowMod) {
            navigation(StarRoute(star.username)) {
                heading3(star.username.value)
            }

            button(modify(HelmBar.IconMod)) {
                onClick = StarHelm.ClosePopover

                image(star.image?.thumb, modify(OpacityHigh, Size100P, BorderRadius50P))
            }
        }

        // calendar route goes here

        navigation(StarDashRoute) { // filler content
            onClick = StarHelm.ClosePopover

            row(RowMod) {
                textBlock("Dashboard")
                icon(SvgFile.Dashboard, HelmBar.IconMod)
            }
        }

        button(onClick = session::signOut) {
            row(RowMod) {
                textBlock("Sign out", modify(WhiteSpaceNoWrap))
                icon(SvgFile.SignOut, HelmBar.IconMod)
            }
        }
    }
}

private fun AppScope.someonePanel(dialog: DialogElement) {
    column(modify(MinWidth32)) {
        row(RowMod) {
            heading3("Someone")
            button(modify(HelmBar.IconMod, FadeLoop)) {
                onClick = StarHelm.ClosePopover

                image(SvgFile.Someone, modify(OpacityHigh, Size100P, BorderRadius50P))
            }
        }

        button("sign in", onClick = dialog::open)

//        tabs(Id("someone-tabs")) {
//            tab("Sign in") {
//                gateForm()
//            }
//            tab("Alternative") {
//                // calendar route goes here
//                navigation(SiteConfigRoute) { // filler content
//                    onClick = StarHelmKey.ClosePopover
//
//                    row(RowMod + Width32) {
//                        textBlock("Using Streetlight while signed-out", modify(TextAlignRight))
//                        icon(SvgFile.Info, HelmBarKey.IconMod)
//                    }
//                }
//
//                navigation(SiteConfigRoute) { // filler content
//                    onClick = StarHelmKey.ClosePopover
//
//                    row(RowMod) {
//                        textBlock("Settings")
//                        icon(SvgFile.Settings, HelmBarKey.IconMod)
//                    }
//                }
//            }
//        }
    }
}

fun AppScope.gateForm() {
    val gate = app.get<StarSession>()
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
            button("sign in", mod = modify(Accent), onClickEvent = {
                gate.signIn()
            })
        }
    }
}

