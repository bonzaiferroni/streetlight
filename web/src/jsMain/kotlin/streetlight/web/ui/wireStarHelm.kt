package streetlight.web.ui

import koala.Svg
import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import koala.html.AppRoute
import koala.html.button
import koala.html.heading3
import koala.html.icon
import koala.html.image
import kampfire.model.setTrue
import koala.html.closePopoverOnClick
import koala.interop.toggleRootModifierWithTransition
import kotlinx.html.onClick
import streetlight.model.data.Star
import streetlight.model.ui.InboxRoute
import streetlight.model.ui.StarDashRoute
import streetlight.model.ui.StarRoute
import streetlight.model.ui.StarConfigRoute
import streetlight.web.model.SessionClient
import streetlight.web.pages.AppOverlay
import streetlight.web.pages.HelmBar
import streetlight.web.pages.StarHelm
import web.dom.document
import web.html.HTMLElement

fun ViewScope.wireStarHelm() {
    val barElement = document.body.querySelector(StarHelm.StarBarHelm) ?: error("star helm content not found")
    wireStarHelm(barElement)
    val panelElement = document.body.querySelector(StarHelm.StarPanelHelm) ?: error("star bar element not found")
    wireStarHelm(panelElement)
}

fun ViewScope.wireStarHelm(element: HTMLElement) {
    wireBlock("wire-star-nav", element) {
        starGate(
            baseContent = {
                someonePanel()
            }
        ) { star ->
            starPanel(star)
        }
    }
}

private val RowMod = modify(AlignItemsCenter, PaddingLeft(3), JustifyContentEnd)

private fun ViewScope.starPanel(star: Star) {
    val session = app.get<SessionClient>()

    column(AlignItemsEnd) {
        row(RowMod) {
            navigation(StarRoute(star.username)) {
                closePopoverOnClick(StarHelm.PopoverId)

                heading3(star.username.value)
            }

            button(HelmBar.IconMod) {
                closePopoverOnClick(StarHelm.PopoverId)

                image(star.image?.thumb, modify(OpacityHigh, Size100P, BorderRadius50P))
            }
        }

        // calendar route goes here
        routeItem(StarDashRoute, "Dashboard", SvgFile.Dashboard)
        routeItem(StarConfigRoute, "Account", SvgFile.User)
        routeItem(InboxRoute, "Inbox", SvgFile.MailLarge)

        button({
            toggleRootModifierWithTransition(AppOverlay.RevealRightPanel.identifier)
        }) {
            closePopoverOnClick(StarHelm.PopoverId)

            row(RowMod) {
                textBlock("Pin menu")
                icon(SvgFile.PanelRight, HelmBar.IconMod)
            }
        }

        button(session::signOut) {
            closePopoverOnClick(StarHelm.PopoverId)

            row(RowMod) {
                textBlock("Sign out", WhiteSpaceNoWrap)
                icon(SvgFile.SignOut, HelmBar.IconMod)
            }
        }
    }
}

private fun ViewScope.routeItem(route: AppRoute, text: String, svg: Svg) {
    navigation(route) { // filler content
        closePopoverOnClick(StarHelm.PopoverId)

        row(RowMod) {
            textBlock(text)
            icon(svg, HelmBar.IconMod)
        }
    }
}

private fun ViewScope.someonePanel() {
    column(MinWidth(32)) {
        row(RowMod) {
            heading3("Someone")
            button(modify(HelmBar.IconMod, FadeLoop)) {
                onClick = StarHelm.ClosePopover.block

                image(SvgFile.Someone, modify(OpacityHigh, Size100P, BorderRadius50P))
            }
        }

        button("sign in", onClick = SignIn.isOpen::setTrue)

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

// fun AppScope.gateForm() {
//     val gate = app.get<StarSession>()
//     val cred = app.get<CredentialStore>()
//
//     column {
//         textField(
//             label = "username/email",
//             onValue = cred::setUsername,
//             placeholder = "username/email",
//             flow = cred.usernameFlow
//         ) {
//             autoComplete = "username"
//         }
//         textField(
//             label = "password",
//             onValue = cred::setPassword,
//             placeholder = "password",
//             flow = cred.passwordFlow
//         ) {
//             type = InputType.password
//             autoComplete = "password"
//         }
//         checkBox("Stay signed in", cred::setStayLoggedIn, cred.stayLoggedInFlow)
//         row {
//             button("sign in", mod = modify(Accent), onClickEvent = {
//                 gate.signIn()
//             })
//         }
//     }
// }

