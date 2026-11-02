package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import streetlight.web.model.MediaEditor
import streetlight.web.model.AudioSource
import streetlight.model.data.MediaType
import koala.dom.layout.flowBlock
import koala.dom.component.*
import koala.dom.markdown.*
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection

context(model: MediaEditor)
fun ViewScope.mediaForm() = formCard {
    formSection("type") {
        switchMenu(model.mediaTypeField)
    }

    formRow {
        formSection("title") {
            textField(model.titleField, "title")
            textField(model.subtitleField, "subtitle")
        }
        formSection("image") {
            imageDrop(model.imageEditor.imageField, modify(Aspect3By2, BorderRadius1))
        }
    }

    flowBlock(model.mediaTypeField) { mediaType ->
        if (mediaType == MediaType.Audio) formSection("audio") {
            switchMenu(model.audioEditor.sourceField)
            flowBlock(model.audioEditor.sourceField) { source ->
                when (source) {
                    AudioSource.File -> audioDrop(model.audioEditor.urlField)
                    AudioSource.Record -> recordAudio(model.audioEditor.urlField)
                }
            }
        }
    }

    formSection("content") {
        markdownEditor(model.textField, "content", MinHeight(32))
    }

    context(model.skillLinkEditor) {
        skillLinksSection()
    }
}
