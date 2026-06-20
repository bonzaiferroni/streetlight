package streetlight.web.ui

import koala.css.*
import koala.html.enumAttributeOf
import streetlight.model.utils.TextDeltaDisplay

object TextDeltaStyle {
    val CommonText = Class("common-text")
    val AddedText = Class("added-text")
    val RemovedText = Class("removed-text")

    val Display = enumAttributeOf<TextDeltaDisplay>("display")
}

//language="CSS"
val TextDeltaCss get() = with(TextDeltaStyle) {"""

$AddedText {
    color: green;
}

$RemovedText {
    color: red;
}

${Display.selector(TextDeltaDisplay.Removed)} $AddedText,
${Display.selector(TextDeltaDisplay.Added)} $RemovedText {
     display: none;
}

""" }