@file:OptIn(FlowPreview::class)

package streetlight.web.ui

import kampfire.model.Url
import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.web.model.Streetlight
import streetlight.web.pages.appFooter

fun ViewContext<Streetlight>.viewStarEditor() {
    starBlock(model, true) { user ->
        val state = storeOf(user)

        val avatarFlow = state.flow.mapDistinct { it.imageRef }
        val usernameFlow = state.flow.mapDistinct { it.username }
        val isAvailableFlow = usernameFlow.debounce(500).map {
            if (it == user.username) null
            else api.checkUsername(it)
        }

        fun setUsername(value: String) = state.set { it.copy(username = value) }
        fun setImageRef(value: Url?) = state.set { it.copy(imageRef = value) }
        fun update() {
            var user = state.now // td: check validity?
            val blobUrl = user.imageRef?.takeIf { it.isBlob }
            renderScope.launch {
                user = if (blobUrl != null) {
                    val refUrl = api.uploadAvatar(blobUrl) ?: error("error creating avatar")
                    console.log(refUrl)
                    user.copy(imageRef = refUrl)
                } else user

//                val isSuccess = api.updateUser(user) ?: return@launch
//                if (isSuccess) {
//                    model.gate.setUpdate(user)
//                }
            }
        }

        column {
            card {
                row(modify(AlignItemsStart)) {
                    imageDrop(avatarFlow, ::setImageRef, modify(Width16, AspectRatio1))
                    row {
                        textField("username", onValue = ::setUsername, flow = usernameFlow)
                        flowBlock(isAvailableFlow, defaultMagic) {
                            val isAvailable = it ?: return@flowBlock
                            val text = if (isAvailable) "👍" else "❌"
                            textBlock(text)
                        }
                    }
                }
                row(modify(JustifyContentSpaceBetween)) {
                    button("cancel", onClick = { portal.goBack() })
                    button("update", modify(Accent), onClick = ::update)
                }
            }

            appFooter()
        }
    }
}