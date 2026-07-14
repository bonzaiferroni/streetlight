package streetlight.web.ui

import koala.dom.*
import koala.model.tap
import streetlight.model.data.TalentEdit
import streetlight.model.data.toEdit
import streetlight.model.ui.EditTalentRoute
import streetlight.web.model.DataCache

fun AppScope.editTalentForm() {
    val userCache = app.get<DataCache>()

    suspend fun provideEdit(route: EditTalentRoute) = route.talentId?.let {
        userCache.talent.getItem(it)?.toEdit()
    } ?: TalentEdit()

    wireRouteTo(portal, TalentEdit(), ::provideEdit) {
        column {
//            textField(
//                label = "talent",
//                read = { it.name },
//                write = { it.state.copy(name = it.value) }
//            )
//            textField(
//                label = "description",
//                read = { it.description },
//                write = { it.state.copy(description = it.value) }
//            )
            dropMenu(
                onChangeValue = { value -> state.set { it.copy(talentType = value) } },
                provideLabel = { it.name },
                flow = state.flow.tap { it.talentType }
            )

//            button(
//                text = { if (it.talentId != null) "edit" else "share" },
//                modifiers = modify(Accent),
//                onClick = {
//                    renderScope.launch {
//                        val talent = api.editTalent(state.now) ?: return@launch
//                        console.log("edit talent: ${talent.name}")
//                        userCache.talent.addItem(talent)
//                        portal.goBack()
//                    }
//                }
//            )
        }
    }
}

//data class NewTalent(
//    val name: String,
//    val description: String?,
//    val imageUrl: String?,
//    val talentType: TalentType,
//)