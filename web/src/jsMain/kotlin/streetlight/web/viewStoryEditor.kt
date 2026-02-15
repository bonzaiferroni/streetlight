package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.geoMapMount
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

fun RenderContext.viewStoryEditor(app: AppContext) {
    val model = app.storyEditor

    val element = column(modify(FlexItems1)) {
        row {
            textField(
                label = "url",
                onChangeValue = model::setUrl,
                binding = model.infoUrlFlow,
                modifiers = modify(Flex1),
                placeholder = "Info link"
            )
            button("read", onClick = model::readUrl)
        }
        image(
            src = model.stateNow.story.imageUrl,
            binding = model.imageUrlFlow
        )
        textField(
            label = "headline",
            onChangeValue = model::setHeadline,
            binding = model.headlineFlow,
            placeholder = "Story Headline"
        )
        textEditor(
            label = "description",
            onChangeValue = model::setDescription,
            binding = model.descriptionFlow,
            placeholder = "Story description"
        )
        textBlock(model.locationFlow.map { it.toString() })
        textBlock(model.postedAtFlow.map { it.toString() })
        row(modify(FlexItems1)) {
            geoMapMount()
            column { }
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
        app.portal.routeFlowOf<EditStoryRoute>().collect {
            model.initStory(it.storyId)
        }
    }

    wireGeoMap(app.geoMap, app.appScope, element)
}