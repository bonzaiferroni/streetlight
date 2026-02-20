package streetlight.web

import koala.dom.RenderContext
import koala.dom.column
import koala.dom.textField
import koala.model.mapDistinct
import koala.model.stateOf
import streetlight.model.data.NewTalent

fun RenderContext.shareTalentForm(app: AppContext) {
    val state = stateOf(NewTalent())

    column {
//        textField(
//            label = "talent",
//            modelState = state,
//            provideValue = { it.name },
//            writeValue = {  }
//        )
    }
}

//data class NewTalent(
//    val name: String,
//    val description: String?,
//    val imageUrl: String?,
//    val talentType: TalentType,
//)