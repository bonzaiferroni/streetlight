package streetlight.web.ui

import kampfire.model.storeOf
import koala.LottieFile
import koala.modifier.MarginTop1
import koala.modifier.modify
import koala.dom.*
import kotlinx.css.GridTemplateColumns

fun RouteScope.viewSandbox() {
    // val model = Sandbox(contentScope, api, toaster)

    console.log("welcome to sandbox")

    val nameState = storeOf("Jimmy")
    val indexState = storeOf(0)

    grid(GridTemplateColumns("1fr 1fr"), modify(MarginTop1)) {
        column {
            lazyTabs(indexState = indexState) {
                tab("original") {
                    lottie(LottieFile.Math)
                }
                tab("original") {
                    lottie(LottieFile.CupShuffleProto)
                }
                tab("original") {
                    lottie(LottieFile.CupShuffleProto)
                }
            }
            textBlock("select a tab")
        }

        column {
            tabs(indexState = indexState) {
                tab("mod 1") {
                    lottie(LottieFile.CupShuffle)
                }
                tab("mod 2") {
                    lottie(LottieFile.CupShuffle)
                }
                tab("mod 3") {
                    lottie(LottieFile.CupShuffle)
                }
            }
            textBlock("select a tab")
        }
    }
}

class SandboxException : Exception("There's a snake in my boot!")

