package streetlight.web

import koala.css.MagicBlur
import koala.css.MagicSlideX
import koala.css.Width64
import koala.css.modify
import koala.dom.*
import koala.model.mapDistinct

fun RenderContext.eventParserDialog(model: EventParser) {
    val dialog = dialogBox("Event reader", model.state.flow.mapDistinct { it.isOpen }, modify(Width64)) {
        messageBox(model.message.flow, modify(MagicSlideX, MagicBlur))
        flowBlock(model.state.flow.mapDistinct { it.parse }) { parse ->
            val events = parse?.events ?: return@flowBlock
            column {
                events.forEachIndexed { index, event ->
                    val eventName = event.name ?: return@forEachIndexed
                    val date = event.date ?: return@forEachIndexed
                    val time = event.time
                    val itemElement = card {
                        textBlock(eventName)
                        row {
                            textBlock(date.toString())
                            time?.let {
                                textBlock(time.toString())
                            }
                        }
                    }

                    itemElement.onClick { model.select(index) }
                }
            }
        }
    }
}