package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.textBlock
import koala.modifier.transition
import streetlight.model.music.MusicStudy
import streetlight.model.music.Study
import streetlight.model.ui.MusicStudioIndexRoute
import streetlight.web.dom.layout.topSection
import streetlight.web.model.portal

fun ViewScope.studyFocus(study: MusicStudy) {
    topSection(
        label = study.label,
        onBack = { portal.go(MusicStudioIndexRoute) },
        headingMod = studyLabelTransition(study.study),
    ) {
        textBlock("hello ${study.label}")
    }
}

fun studyLabelTransition(study: Study) = transition("study-${study.trackId}-${study.notationId ?: "track"}")
