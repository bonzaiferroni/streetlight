package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.heading2
import koala.html.section
import koala.html.textBlock
import koala.model.storeOf
import kotlinx.coroutines.launch
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.ExtraLink
import streetlight.model.data.toEdit
import streetlight.web.EditEventCallbackRoute
import streetlight.web.EditEventIdRoute
import streetlight.web.EditEventRoute

// event editor content including introduction and form
fun RenderContext.viewEventEditorProto(
    event: EventEdit,
    callback: ((Event?) -> Unit)?
) {
    val msg = storeOf<UIMessage?>(null)
    val editStore = storeOf(event)

    // give feedback on edit validity
    renderScope.launch {
//        editStore.flow.collect { edit ->
//            when (val text = edit.invalidMessage) {
//                null -> msg.set("Looks good.")
//                else -> msg.set(text)
//            }
//        }
    }

    val sectionMod = modify(QueryContainer)

    column {
        section(sectionMod) {
            filigree {
                heading1("Event Editor", modify(Shrinkable))
            }
            column(modify(ContainerMdRow, AlignItemsCenter)) {
                row(modify(Flex2, JustifyContentCenter, MaxWidth48)) {
                    lottie(LottieFile.Wombat)
                }
                column(modify(Flex3, FlexMd2, PaddingLeft3)) {
                    textBlock(introText1)
                    textBlock(introText2)
                }
            }
        }

        section {
            filigree {
                heading2("Event Details")
            }
            card {
                viewEventFormProto(event, editStore.flow, editStore::setValue)
            }
        }

        row(modify(JustifyContentSpaceBetween, PaddingX1)) {
            button("cancel", modify(Secondary), onClick = {
                portal.goBack()
            })
            row {
                messageBox(msg.flow, modify(Flex1))
                val text = when (event.eventId) {
                    null -> "create"
                    else -> "edit"
                }
                button(text, modify(Accent), onClick = {
                    renderScope.launch {
//                        api.createOrEditEvent(editStore.now).handleResponse(msg::set) {
//                            if (callback != null) {
//                                portal.goBack()
//                                callback.invoke(it)
//                            } else {
//                                portal.go(it.route)
//                            }
//                        }
                    }
                })
            }
        }

        appFooter(
            "web/src/jsMain/kotlin/streetlight/web/ui/viewEventEditor.kt",
            ExtraLink("eventEditorForm.kt", "web/src/jsMain/kotlin/streetlight/web/ui/eventEditorForm.kt")
        )
    }
}

// event editor route, invoked by appNavigation
fun RenderContext.viewEventEditorRoute() {
    var callback: ((Event?) -> Unit)? = null

    routeBlock<EditEventRoute, EventEdit>({ route ->
        when (route) {
            // start with existing event or a blank slate to create a new event
            is EditEventIdRoute -> route.eventId?.let {
                api.readEvent(it).handleResponse(toaster::toast) { event ->
                    event.toEdit()
                }
            } ?: EventEdit()
            // for when the edit can be determined clientside by a view that consumes the edit
            is EditEventCallbackRoute -> {
                callback = route.callback
                route.event
            }
        }
    }) { event ->
        viewEventEditorProto(event, callback)
    }
}

private val introText1 = """
Streetlight follows in the footsteps of Wikipedia. If you know about an upcoming event, share the details.
"""

private val introText2 = """
You may also edit the details of an existing event if you have additional information or something has changed.
"""