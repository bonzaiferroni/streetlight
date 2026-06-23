package streetlight.web.ui

import koala.css.*
import koala.html.enumAttributeOf
import streetlight.model.utils.TextDeltaDisplay

object TextDeltaStyle {
    val CommonText = Class("common-text")
    val AddedText = Class("added-text")
    val RemovedText = Class("removed-text")
    val Highlighter = Class("highlighter")

    val Display = enumAttributeOf<TextDeltaDisplay>("display")
}

//language="CSS"
val TextDeltaCss get() = with(TextDeltaStyle) {"""

$Highlighter {
    $AddedText {
        color: var(--green-fg);
    }
    
    $RemovedText {
        color: var(--red-fg);
    }
}

${Display.selector(TextDeltaDisplay.Removed)} $AddedText,
${Display.selector(TextDeltaDisplay.Added)} $RemovedText {
     display: none;
}

""" }