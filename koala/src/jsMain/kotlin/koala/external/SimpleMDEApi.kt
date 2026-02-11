@file:Suppress("unused")

package koala.external

import kotlinx.js.JsPlainObject
import kotlin.coroutines.CoroutineContext

import org.w3c.dom.HTMLElement

// -------- SimpleMDE core --------

external class SimpleMDE(options: SimpleMDEOptions = definedExternally) {
    fun value(): String
    fun value(value: String)

    fun togglePreview()
    fun toggleSideBySide()
    fun toggleFullScreen()

    fun isPreviewActive(): Boolean
    fun isSideBySideActive(): Boolean
    fun isFullscreenActive(): Boolean

    fun toTextArea()
}

// -------- Options bags (plain JS objects) --------

@JsPlainObject
external interface SimpleMDEOptions {
    var autofocus: Boolean?
    var autosave: SimpleMDEAutosaveOptions?
    var blockStyles: SimpleMDEBlockStyles?
    var element: HTMLElement?
    var forceSync: Boolean?
    var hideIcons: Array<String>?
    var indentWithTabs: Boolean?
    var initialValue: String?
    var insertTexts: SimpleMDEInsertTexts?
    var lineWrapping: Boolean?
    var parsingConfig: SimpleMDEParsingConfig?
    var placeholder: String?

    /**
     * SimpleMDE supports both sync and async previewRender signatures.
     * - (plainText) => String
     * - (plainText, preview) => String
     */
    var previewRender: ((plainText: String, preview: HTMLElement?) -> String)?

    var promptURLs: Boolean?
    var renderingConfig: SimpleMDERenderingConfig?
    var shortcuts: SimpleMDEShortcuts?
    var showIcons: Array<String>?
    var spellChecker: Boolean?
    var status: dynamic // false | Array<String|StatusItem>
    var styleSelectedText: Boolean?
    var tabSize: Int?
    var toolbar: dynamic // false | Array<...> | "..." (kept dynamic for breadth)
    var toolbarTips: Boolean?
}

@JsPlainObject
external interface SimpleMDEAutosaveOptions {
    var enabled: Boolean?
    var uniqueId: String?
    var delay: Int?
}

@JsPlainObject
external interface SimpleMDEBlockStyles {
    var bold: String?
    var italic: String?
    var code: String?
    var quote: String?
}

@JsPlainObject
external interface SimpleMDEInsertTexts {
    var horizontalRule: Array<String>?
    var image: Array<String>?
    var link: Array<String>?
    var table: Array<String>?
}

@JsPlainObject
external interface SimpleMDEParsingConfig {
    var allowAtxHeaderWithoutSpace: Boolean?
    var strikethrough: Boolean?
    var underscoresBreakWords: Boolean?
}

@JsPlainObject
external interface SimpleMDERenderingConfig {
    var singleLineBreaks: Boolean?
    var codeSyntaxHighlighting: Boolean?
}

@JsPlainObject
external interface SimpleMDEShortcuts {
    var drawTable: String?
}

// Optional: custom status-bar item shape (when status includes objects)
@JsPlainObject
external interface SimpleMDEStatusItem {
    var className: String?
    var defaultValue: ((el: HTMLElement) -> Unit)?
    var onUpdate: ((el: HTMLElement) -> Unit)?
}