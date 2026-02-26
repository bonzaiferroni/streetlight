package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.geoMapMount
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

fun RenderContext.viewPostEditor(app: AppContext) {
    val model = PostEditor(renderScope, app.client, app.geoMap)

    val element = column(modify(FlexItems1)) {
        row {
            textField(
                label = "url",
                onChangeValue = model::setUrl,
                values = model.infoUrlFlow,
                modifiers = modify(Flex1),
                placeholder = "Info link"
            )
            button("read", onClick = model::readUrl)
        }
        column(modify(QueryRow)) {
            image (
                modifiers = modify(Flex1),
                binding = model.imageUrlFlow
            )
            column(modify(Flex2, AlignItemsStretch)) {
                textField(
                    label = "headline",
                    onChangeValue = model::setHeadline,
                    values = model.headlineFlow,
                    placeholder = "Story Headline"
                )
                textEditor(
                    label = "description",
                    onChangeValue = model::setDescription,
                    binding = model.descriptionFlow,
                    placeholder = "Story description"
                )
                textBlock(model.postedAtFlow.map { it.toString() })
            }
        }
        column(modify(QueryRow)) {
            geoMapMount(null, modify(Flex1, Square))
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