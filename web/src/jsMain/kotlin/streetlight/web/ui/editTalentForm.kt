package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import kotlinx.coroutines.launch
import streetlight.model.data.TalentEdit
import streetlight.model.data.toEdit
import streetlight.web.EditTalentRoute

fun RenderContext.editTalentForm(app: AppContext) {
    val portal = app.portal
    val api = app.client.api
    val userCache = app.userCache

    suspend fun provideEdit(route: EditTalentRoute) = route.talentId?.let {
        userCache.talents.getItem(it)?.toEdit()
    } ?: TalentEdit()

    wireRouteTo(portal, TalentEdit(), ::provideEdit) {
        column {
            textField(
                label = "talent",
                read = { it.name },
                write = { it.state.copy(name = it.value) }
            )
            textField(
                label = "description",
                read = { it.description },
                write = { it.state.copy(description = it.value) }
            )
            dropMenu(
                onChangeValue = { value -> state.set { it.copy(talentType = value) } },
                flow = state.flow.mapDistinct { it.talentType },
                provideLabel = { it.name }
            )

            button(
                text = { if (it.talentId != null) "edit" else "share" },
                modifiers = modify(Accent),
                onClick = {
                    renderScope.launch {
                        val talent = api.editTalent(state.now) ?: return@launch
                        console.log("edit talent: ${talent.name}")
                        userCache.talents.addItem(talent)
                        portal.goBack()
                    }
                }
            )
        }
    }
}

//data class NewTalent(
//    val name: String,
//    val description: String?,
//    val imageUrl: String?,
//    val talentType: TalentType,
//)