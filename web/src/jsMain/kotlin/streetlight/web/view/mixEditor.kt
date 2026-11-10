package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.layout.selectorBlock
import koala.dom.layout.selectorContainer
import koala.modifier.*
import kotlinx.css.Flex
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.formRow
import streetlight.web.model.MixEditor
import streetlight.web.model.SectionClipEditor
import streetlight.web.model.SectionMic
import streetlight.web.model.SectionMicPhase
import streetlight.web.model.TrackSection
import streetlight.web.model.getSectionClipEditor
import streetlight.web.model.getSectionMic

/**
 * Renders the open mix: its name, tempo and sounding key, the sections of every track with the clip each plays, and
 * the selected section's notation, the clip bound to it and the recorder of its takes.
 */
context(model: MixEditor)
fun ViewScope.mixEditor() = flowBlock(model.mixState) { mix ->
    if (mix == null) return@flowBlock
    actionSection(mix.name) {
        formRow {
            column {
                row {
                    textField(model.bpmTextState, "tempo", size = 4)
                    dropMenu(MixEditor.TONICS, model.tonicState)
                    dropMenu(MixEditor.MODES, model.modeState)
                }
            }

            selectorContainer {
                selectorBlock(model.sectionsState, model.selectionState) { section ->
                    row(modify(AlignItemsCenter, Padding(1))) {
                        textBlock("${section.trackId.name} · ${section.sectionId.name}", Flex1)
                        flowBlock(model.statusOf(section)) { status -> textBlock(status, modify(TextSmall, InkDimFg)) }
                    }
                }
            }
        }
        flowBlock(model.selectionState) { section ->
            section ?: return@flowBlock
            sectionWork(section)
        }
    }
}

/** Renders the notation of [section], then the choice of its clip and the recorder of its takes. */
context(model: MixEditor)
private fun ViewScope.sectionWork(section: TrackSection) {
    val clipEditor = app.getSectionClipEditor(contentScope, model, section.trackId, section.sectionId)
    val mic = app.getSectionMic(contentScope, model, section.trackId, section.sectionId)
    actionSection(label = "${section.trackId.name} · ${section.sectionId.name}") {
        formRow {
            flowBlock(model.notationOf(section)) { notation ->
                notation ?: return@flowBlock
                sectionStrips(notation)
            }

            column {
                sectionClipPicker(clipEditor)
                sectionRecorder(mic)
            }
        }
    }
}

/** The choice of the clip [clipEditor] binds to its section, or none. */
private fun ViewScope.sectionClipPicker(clipEditor: SectionClipEditor) {
    flowBlock(clipEditor.namesState, modify(PaddingY(1))) { names ->
        dropMenu(names, clipEditor.selectedNameState, Flex1)
    }
}

/**
 * Records takes through [mic]: the lead-in's progress beside the take's, then the name the take is kept under and
 * the button that starts it, or stops it early and discards it.
 */
private fun ViewScope.sectionRecorder(mic: SectionMic) {
    column(modify(PaddingY(1))) {
        row(AlignItemsCenter) {
            progressBar(mic.leadProgressState, modify(Flex1, Height(2), ZenBg), isSmooth = false)
            progressBar(mic.takeProgressState, modify(Css.Flex.of(Flex(4)), Height(2), ZenBg), isSmooth = false)
        }
        row(AlignItemsCenter) {
            textField(mic.nameState, "name", Flex1)
            flowBlock(mic.phaseState) { phase ->
                val label = when (phase) {
                    SectionMicPhase.Idle -> "Record"
                    SectionMicPhase.LeadIn, SectionMicPhase.Recording -> "Stop"
                    SectionMicPhase.Saving -> "Saving"
                }
                button(label, { mic.toggle() })
            }
        }
        switch("Click", mic.isClickOnState)
        flowBlock(mic.isCalibratedState) { isCalibrated ->
            if (isCalibrated) return@flowBlock
            row(AlignItemsCenter) {
                textBlock(
                    "A calibration measures how late sound reaches the mic, so takes line up with the beat.",
                    modify(Flex1, TextSmall, InkDimFg),
                )
                flowBlock(mic.isCalibratingState) { isCalibrating ->
                    button(if (isCalibrating) "Calibrating" else "Calibrate", { mic.calibrate() })
                }
            }
        }
    }
}
