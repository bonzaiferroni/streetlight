@file:OptIn(FlowPreview::class)

package streetlight.web.ui

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

fun ViewContext<Streetlight>.viewProfileEditor() {
    userBlock(model, true) { user ->
        val state = storeOf(user)

        val avatarFlow = state.flow.mapDistinct { it.avatarUrl }
        val usernameFlow = state.flow.mapDistinct { it.username }
        val isAvailableFlow = usernameFlow.debounce(500).map {
            if (it == user.username) null
            else api.checkUsername(it)
        }

        fun setUsername(value: String) = state.set { it.copy(username = value) }
        fun setAvatar(value: String?) = state.set { it.copy(avatarUrl = value) }
        fun update() {
            var user = state.now // td: check validity?
            val blobUrl = user.avatarUrl?.takeIf { it.startsWith("blob:") }
            renderScope.launch {
                user = if (blobUrl != null) {
                    val avatarUrl = api.uploadAvatar(blobUrl) ?: error("error creating avatar")
                    console.log(avatarUrl)
                    user.copy(avatarUrl = avatarUrl)
                } else user

                val isSuccess = api.updateUser(user) ?: return@launch
                if (isSuccess) {
                    model.gate.setUpdate(user)
                }
            }
        }

        column {
            card {
                row(modify(AlignItemsStart)) {
                    imageDrop(avatarFlow, ::setAvatar, modify(Width16, AspectRatio1))
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