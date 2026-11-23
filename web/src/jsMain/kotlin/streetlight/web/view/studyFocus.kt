package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.layout.flowBlock
import koala.modifier.transition
import streetlight.model.music.MusicStudy
import streetlight.model.music.Study
import streetlight.model.ui.MusicStudioIndexRoute
import streetlight.web.dom.layout.topSection
import streetlight.web.model.MusicConfig
import streetlight.web.model.StudySession
import streetlight.web.model.portal

context(config: MusicConfig)
fun ViewScope.studyFocus(study: MusicStudy) {
    val session = StudySession(contentScope, study)
    topSection(
        label = study.label,
        onBack = { portal.go(MusicStudioIndexRoute) },
        headingMod = studyLabelTransition(study.study),
    ) {
        flowBlock(session.layoutsState) { layouts ->
            layouts.forEach { notationStrips(it) }
        }
    }
}

fun studyLabelTransition(study: Study) = transition("study-${study.trackId}-${study.notationId ?: "track"}")
