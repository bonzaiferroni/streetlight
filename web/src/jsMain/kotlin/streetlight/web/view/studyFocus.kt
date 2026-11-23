package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.textBlock
import streetlight.model.music.MusicStudy

fun ViewScope.studyFocus(study: MusicStudy) {
    textBlock("hello ${study.label}")
}
