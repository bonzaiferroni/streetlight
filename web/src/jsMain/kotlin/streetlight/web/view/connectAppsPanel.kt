package streetlight.web.view

import kampfire.model.toUrl
import koala.modifier.*
import koala.dom.*
import koala.component.listItem
import koala.component.olist
import streetlight.web.model.AppConnector
import koala.dom.component.*
import koala.dom.layout.*
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection

fun ViewScope.connectAppsPanel() {
    val model = AppConnector()
    formRow {
        formSection("PayPal") {
            olist {
                listItem {
                    +"Visit "
                    navigation("https://paypal.me", "paypal.me")
                    +" to create a public link"
                }
                listItem {
                    +"Enter the username you chose in the field below."
                }
                listItem {
                    +"Payments go directly to you, Streetlight just provides the link."
                }
            }
            row {
                textField(model.paypalIdState, "PayPal username", Flex1)
                flowBlock(model.paypalIdState) { paypalId ->
                    if (paypalId.isEmpty()) {
                        button("test link", null, Attribute.Disabled.to(Unit))
                    } else {
                        val url = "https://paypal.me/${paypalId}".toUrl()
                        button("Test Link", url)
                    }
                }
            }
        }
        formSection("Venmo") {
            row {
                textField(model.venmoIdState, "Venmo", Flex1)
                flowBlock(model.venmoIdState) { venmoId ->
                    if (venmoId.isEmpty()) {
                        button("test link", null, Attribute.Disabled.to(Unit))
                    } else {
                        val url = "https://venmo.com/${venmoId}".toUrl()
                        button("Test Link", url)
                    }
                }
            }
        }
        formSection("Cash App") {
            row {
                textField(model.cashAppIdState, "Cash App", Flex1)
                flowBlock(model.cashAppIdState) { cashAppId ->
                    if (cashAppId.isEmpty()) {
                        button("test link", null, Attribute.Disabled.to(Unit))
                    } else {
                        val url = "https://cash.app/${cashAppId}".toUrl()
                        button("Test Link", url)
                    }
                }
            }
        }
    }
}
