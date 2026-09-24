package streetlight.web.ui

import koala.modifier.*
import koala.modifier.enumAttributeOf
import streetlight.model.utils.TextDeltaDisplay

/** Shows the difference between two texts; [Display] hides the removed or the added text. */
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