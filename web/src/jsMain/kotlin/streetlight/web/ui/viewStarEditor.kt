@file:OptIn(FlowPreview::class)

package streetlight.web.ui

import kampfire.api.toUsername
import kampfire.model.Url
import kampfire.model.getDataOrNull
import kampfire.model.handleOutcome
import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.data.toEdit
import streetlight.web.model.StarSession
import streetlight.web.pages.appFooter
import kotlin.time.Duration.Companion.milliseconds

fun AppScope.viewStarEditor() {
    val gate = app.get<StarSession>()

    starBlock(true) { star ->
        val state = storeOf(star.toEdit())

        val avatarFlow = state.flow.mapDistinct { it.imageRef }
        val nameFlow = state.flow.mapDistinct { it.username?.value }
        val isAvailableFlow = nameFlow.debounce(500.milliseconds).map {
            if (it == null || it == star.username.value) null
            else api.checkUsername(it.toUsername()).handleOutcome(toaster::toast)
        }

        fun setUsername(value: String) = state.set { it.copy(username = value.toUsername()) }
        fun setImageRef(value: Url?) = state.set { it.copy(imageRef = value) }
        fun update() {
            var edit = state.now // td: check validity?
            val blobUrl = edit.imageRef?.takeIf { it.isBlob }
            parentScope.launch {
                edit = if (blobUrl != null) {
                    val refUrl = api.uploadImage(blobUrl).getDataOrNull() ?: error("error creating avatar")
                    edit.copy(imageRef = refUrl)
                } else edit

                console.log(edit.username)
                val star = api.updateStar(edit).handleOutcome(toaster::toast)
                if (star != null) {
                    gate.setUpdate(star)
                }
            }
        }

        column {
            card {
                row(modify(AlignItemsStart)) {
                    imageDrop(avatarFlow, ::setImageRef, modify(Width16, Aspect1))
                    row {
                        textField("username", ::setUsername, nameFlow)
                        flowBlock(isAvailableFlow, defaultMagic) {
                            val isAvailable = it ?: return@flowBlock
                            val text = if (isAvailable) "👍" else "❌"
                            textBlock(text)
                        }
                    }
                }
                row(modify(JustifyContentSpaceBetween)) {
                    button("cancel", { portal.goBack() })
                    button("update", ::update, modify(Accent))
                }
            }

            appFooter()
        }
    }
}