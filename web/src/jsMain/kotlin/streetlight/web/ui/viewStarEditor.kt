@file:OptIn(FlowPreview::class)

package streetlight.web.ui

import kampfire.api.toUsername
import kampfire.model.Url
import kampfire.model.getDataOrNull
import kampfire.model.handleOutcome
import koala.Image
import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import koala.model.storeOf
import koala.toImage
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

        val avatarFlow = state.flow.mapDistinct { it.image }
        val nameFlow = state.flow.mapDistinct { it.username?.value }
        val isAvailableFlow = nameFlow.debounce(500.milliseconds).map {
            if (it == null || it == star.username.value) null
            else api.checkUsername(it.toUsername()).handleOutcome(toaster::toast)
        }

        fun setUsername(value: String) = state.set { it.copy(username = value.toUsername()) }
        fun setImage(value: Image?) = state.set { it.copy(image = value) }
        fun update() {
            var edit = state.now // td: check validity?
            val blobUrl = edit.image?.url?.takeIf { it.isBlob }
            parentScope.launch {
                edit = if (blobUrl != null) {
                    val image = api.uploadImageBlob(blobUrl).getDataOrNull()?.toImage() ?: error("error creating avatar")
                    edit.copy(image = image)
                } else edit

                val star = api.updateStar(edit).handleOutcome(toaster::toast)
                if (star != null) {
                    gate.setUpdate(star)
                }
            }
        }

        column {
            card {
                row(modify(AlignItemsStart)) {
                    imageDrop(avatarFlow, ::setImage, modify(Width16, Aspect1))
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