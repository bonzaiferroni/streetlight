package koala.html

import kotlinx.html.FlowOrMetaDataOrPhrasingContent
import kotlinx.html.script
import kotlinx.html.unsafe

/** A script of [js], written as is. */
fun FlowOrMetaDataOrPhrasingContent.scriptUnsafe(js: String) {
    script {
        unsafe {
            +js
        }
    }
}