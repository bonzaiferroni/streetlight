package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.geoMapMount
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.web.EditPostRoute
import streetlight.web.model.Streetlight
import streetlight.web.model.PostEditor

fun RenderContext.viewPostEditor(app: Streetlight) {
    val model = PostEditor(renderScope, app.client, app.geoMap)

    val element = column(modify(FlexItems1)) {
        row {
            textField(
                label = "url",
                onChangeValue = model::setUrl,
                bindFlow = model.infoUrlFlow,
                modifiers = modify(Flex1),
                placeholder = "Info link"
            )
            button("read", onClick = model::readUrl)
        }
        column(modify(MediaMdRow)) {
            image (
                modifiers = modify(Flex1),
                binding = model.imageUrlFlow
            )
            column(modify(Flex2, AlignItemsStretch)) {
                textField(
                    label = "headline",
                    onChangeValue = model::setHeadline,
                    bindFlow = model.headlineFlow,
                    placeholder = "Story Headline"
                )
                textEditor(
                    label = "description",
                    onChangeValue = model::setDescription,
                    bindFlow = model.descriptionFlow,
                    placeholder = "Story description"
                )
                textBlock(model.postedAtFlow.map { it.toString() })
            }
        }
        column(modify(MediaMdRow)) {
            geoMapMount(null, modify(Flex1, AspectRatio1))
            column(modify(Flex2)) {
                textBlock(model.locationFlow.map { it.toString() })
            }
        }
        row {
            button("cancel", onClickEvent = {
                app.portal.goBack()
            })
            button("create", modify(Accent), onClickEvent = {
                // TODO: model.saveStory()
            })
        }
    }

    renderScope.launch {
        app.portal.routeFlowOf<EditPostRoute>().collect {
            model.initStory(it.postId)
        }
    }

    wireGeoMap(app.geoMap, app.appScope, element)
}