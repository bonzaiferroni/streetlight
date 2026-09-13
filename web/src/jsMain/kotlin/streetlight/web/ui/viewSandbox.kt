package streetlight.web.ui

import kampfire.model.storeOf
import koala.css.MarginTop1
import koala.css.modify
import koala.dom.*
import kotlinx.css.GridTemplateColumns

fun RouteScope.viewSandbox() {
    // val model = Sandbox(contentScope, api, toaster)

    console.log("welcome to sandbox")

    val nameState = storeOf("Jimmy")
    val indexState = storeOf(1)

    grid(GridTemplateColumns("1fr 1fr"), modify(MarginTop1)) {
        column {
            lazyTabs(indexState = indexState) {
                tab("The Dodmand") {
                    textBlock("yer at The Dodmand.")
                    textField(nameState, "yer name")
                    println("haul up")
                }
                tab("Plymouth") {
                    textBlock("yer at Plymouth")
                    flowBlock(nameState) { name ->
                        textBlock("hello $name")
                    }
                    println("yer clewgarnets")
                }
                tab("Start") {
                    textBlock("yer at Start")
                    println("let tacks and sheets fly")
                }
            }
            textBlock("select a tab")
        }

        column {
            tabs(indexState = indexState) {
                tab("The Dodmand") {
                    textBlock("yer at The Dodmand.")
                    textField(nameState, "yer name")
                }
                tab("Plymouth") {
                    textBlock("yer at Plymouth")
                    flowBlock(nameState) { name ->
                        textBlock("hello $name")
                    }
                }
                tab("Start") {
                    textBlock("yer at Start")
                }
            }
            textBlock("select a tab")
        }
    }
}

class SandboxException : Exception("There's a snake in my boot!")

