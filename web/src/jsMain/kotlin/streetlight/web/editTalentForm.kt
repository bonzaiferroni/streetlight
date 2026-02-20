package streetlight.web

import koala.css.*
import koala.dom.*
import koala.model.mapDistinct
import koala.model.stateOf
import kotlinx.coroutines.launch
import streetlight.model.data.TalentEdit
import streetlight.model.data.toEdit

fun RenderContext.editTalentForm(app: AppContext) {
    val portal = app.portal
    val api = app.client.api
    val userCache = app.userCache

    suspend fun provideEdit(route: EditTalentRoute) = route.talentId?.let {
        userCache.talents.getItem(it)?.toEdit()
    } ?: TalentEdit()

    routeBlock(portal, ::provideEdit) {
        val state = stateOf(TalentEdit())
        wireTo(state) {
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
}

//data class NewTalent(
//    val name: String,
//    val description: String?,
//    val imageUrl: String?,
//    val talentType: TalentType,
//)