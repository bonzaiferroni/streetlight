package koala.html

import kotlinx.html.FlowOrMetaDataOrPhrasingContent
import kotlinx.html.script
import kotlinx.html.unsafe

fun FlowOrMetaDataOrPhrasingContent.scriptUnsafe(js: String) {
    script {
        unsafe {
            +js
        }
    }
}