package streetlight.web.view

import koala.SvgFile
import koala.component.IconStyle
import koala.component.heading5
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.layout.selectorBlock
import koala.dom.layout.selectorContainer
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kotlinx.css.Flex
import streetlight.model.music.Mix
import streetlight.model.music.TrackSectionId
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.actionCard
import streetlight.web.dom.layout.formRow
import streetlight.web.model.MixEditor
import streetlight.web.model.MODE_NAMES
import streetlight.web.model.MixForm
import streetlight.web.model.MixSection
import streetlight.web.model.TONIC_NAMES
import streetlight.web.model.SectionClipEditor
import streetlight.web.model.SectionMic
import streetlight.web.model.SectionMicPhase
import streetlight.web.model.getSectionClipEditor
import streetlight.web.model.getSectionMic
import kotlin.uuid.Uuid

/**
 * Renders the open mix under its name, with actions to edit its properties and to select or create another: its tempo
 * and sounding key beside the way to its hidden sections, then a card for each section it shows, or the section in
 * focus alone.
 */
context(model: MixEditor)
fun ViewScope.mixEditor() = flowBlock(model.propertiesState) { mix ->
    if (mix == null) return@flowBlock
    val editId = Id(Uuid.random().toString())
    val selectId = Id(Uuid.random().toString())
    val hiddenId = Id(Uuid.random().toString())
    mixEditPopover(editId, mix)
    mixSelectPopover(selectId)
    hiddenSectionsPopover(hiddenId)
    actionSection(
        label = mix.name,
        actions = listOf(
            MenuAction("Edit", config = { setPopoverTarget(editId, true) }) { },
            MenuAction("Select", config = { setPopoverTarget(selectId, true) }) { },
        ),
    ) {
        row(AlignItemsCenter) {
            textBlock(mix.summary, modify(Flex1, TextSmall, InkDimFg))
            button({ setPopoverTarget(hiddenId, true) }, modify(TextSmall, TextUppercase)) {
                setPopoverTarget(hiddenId, true)
                textBlock("hidden")
            }
        }
        flowBlock(model.focusState, Transitioner) { focus ->
            when (focus) {
                null -> flowBlock(model.shownSectionsState, MixStyle.Cards) { sections ->
                    sections.forEach { sectionCard(it) }
                }
                else -> sectionFocus(focus)
            }
        }
    }
}

/** A popover with [id] holding the sections the open mix hides, each beside the button that shows it again. */
context(model: MixEditor)
private fun ViewScope.hiddenSectionsPopover(id: Id) {
    popover(id) {
        flowBlock(model.hiddenSectionsState, modify(FlexColumn, Padding(2))) { sections ->
            sections.forEach { hidden ->
                row(AlignItemsCenter) {
                    textBlock(hidden.label, Flex1)
                    button("show", { model.showSection(hidden.trackSectionId) }, TextSmall)
                }
            }
            if (sections.isEmpty()) textBlock("No hidden sections.", modify(TextSmall, InkDimFg))
        }
    }
}

/**
 * Renders [mixSection] in a card under its label and its track's icon, with an action that hides it: the slot of its
 * clip, which opens the clips that fit it, beside the button that brings it into focus to record.
 */
context(model: MixEditor)
private fun ViewScope.sectionCard(mixSection: MixSection) {
    val trackSectionId = mixSection.trackSectionId
    val clipEditor = app.getSectionClipEditor(contentScope, model, trackSectionId)
    val clipsId = Id(Uuid.random().toString())
    clipsPopover(clipsId, clipEditor)
    actionCard(
        label = mixSection.label,
        actions = listOf(MenuAction("Hide") { model.hideSection(trackSectionId) }),
        mod = modify(transition(trackSectionId.toKey())),
        svg = model.musicEditor.readTrack(trackSectionId.trackId)?.svg?.small,
    ) {
        card {
            row(AlignItemsCenter) {
                flowBlock(model.clipLabelOf(trackSectionId), Flex1) { label ->
                    button(mod = modify(MixStyle.Slot, VoidBg.takeIf { label == null })) {
                        setPopoverTarget(clipsId, true)
                        +label.orEmpty()
                    }
                }
                val recordMod = modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Record"))
                button(SvgFile.PlayerRecord.filled, { model.focusSection(trackSectionId) }, recordMod)
            }
        }
    }
}

/** A popover with [id] holding the clips [clipEditor] may bind to its section, where a click binds one, or none. */
private fun ViewScope.clipsPopover(id: Id, clipEditor: SectionClipEditor) {
    popover(id) { popover ->
        flowBlock(clipEditor.namesState) { names ->
            actionMenu(names.map { name ->
                MenuAction(name) {
                    popover.close()
                    clipEditor.selectedNameState.set(name)
                }
            })
        }
    }
}

/**
 * Renders [trackSectionId] alone in a card under its label, with a back arrow that returns to the overview: its
 * notation beside the recorder of its takes.
 */
context(model: MixEditor)
private fun ViewScope.sectionFocus(trackSectionId: TrackSectionId) {
    val mic = app.getSectionMic(contentScope, model, trackSectionId)
    card(transition(trackSectionId.toKey())) {
        actionSection(model.labelOf(trackSectionId), onBack = { model.clearFocus() }, heading = { heading5(it) }) {
            formRow {
                flowBlock(model.notationOf(trackSectionId)) { notation -> notation?.let { sectionStrips(it) } }
                sectionRecorder(mic)
            }
        }
    }
}

/** A popover with [id] holding the properties of [mix], which its save button applies. */
context(model: MixEditor)
private fun ViewScope.mixEditPopover(id: Id, mix: Mix) {
    popover(id) { popover ->
        val form = MixForm(mix)
        column(Padding(2)) {
            context(form) { mixForm() }
            button("Save", { if (model.applyMixForm(form)) popover.close() })
        }
    }
}

/** A popover with [id] holding the mixes of the music, where a click opens one, above the form of a new mix. */
context(model: MixEditor)
private fun ViewScope.mixSelectPopover(id: Id) {
    popover(id) { popover ->
        val form = model.newMixForm()
        column(Padding(2)) {
            selectorContainer {
                selectorBlock(model.mixesState, model.openMixState) { mix ->
                    row(modify(AlignItemsCenter, Padding(1))) {
                        textBlock(mix.name, Flex1)
                        textBlock(mix.summary, modify(TextSmall, InkDimFg))
                    }
                }
            }
            context(form) { mixForm() }
            button("Create", { if (model.applyMixForm(form)) popover.close() })
        }
    }
}

/** Renders the fields of a mix's name, tempo and sounding key. */
context(form: MixForm)
private fun ViewScope.mixForm() {
    textField(form.nameState, "name")
    row {
        textField(form.bpmTextState, "tempo", size = 4)
        dropMenu(TONIC_NAMES, form.tonicState)
        dropMenu(MODE_NAMES, form.modeState)
    }
}

/** The tempo and sounding key of this mix, as `90 bpm · G Major`. */
private val Mix.summary get() = "$bpm bpm · ${TONIC_NAMES[soundingKey.tonic.value]} ${soundingKey.mode.name}"

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
