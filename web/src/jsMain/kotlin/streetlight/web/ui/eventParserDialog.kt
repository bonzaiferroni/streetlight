package streetlight.web.ui

//fun RenderContext.eventParserDialog(model: EventParser) {
//    val dialog = dialogBox("Event reader", model.state.flow.mapDistinct { it.isOpen }, modify(Width64)) {
//        messageBox(model.message.flow, modify(SlideX, Blur))
//        flowBlock(model.state.flow.mapDistinct { it.parse }) { parse ->
//            val events = parse?.events ?: return@flowBlock
//            column {
//                events.forEachIndexed { index, event ->
//                    val eventName = event.name ?: return@forEachIndexed
//                    val date = event.date ?: return@forEachIndexed
//                    val time = event.time
//                    val itemElement = card {
//                        row {
//                            event.imageUrl?.takeIf { it.startsWith("http") }?.let {
//                                image(it, modify(Width16))
//                            }
//                            column(modify(Flex1)) {
//                                // name
//                                textBlock(eventName)
//                                // time/date
//                                row {
//                                    textBlock(date.toString())
//                                    time?.let {
//                                        textBlock(time.toString())
//                                    }
//                                }
//                                // location
//                                event.location?.let {
//                                    propertyValue("location", it)
//                                }
//                                // address
//                                event.address?.let {
//                                    propertyValue("address", it)
//                                }
//                                // description
//                                event.description?.let {
//                                    propertyValue("description", it.takeEllipsis(200))
//                                }
//                                // ageMin
//                                event.ageMin?.let {
//                                    propertyValue("ages", "$it+")
//                                }
//                                // contact
//                                event.contact?.let {
//                                    propertyValue("contact", it)
//                                }
//                                // url
//                                event.url?.takeIf { it.startsWith("http") }?.let {
//                                    propertyValue("url", it)
//                                }
//                            }
//                        }
//                    }
//
//                    itemElement.onClick { model.select(index) }
//                }
//            }
//        }
//    }
//}

//    val name: String? = null,
//    val time: LocalTime? = null,
//    val date: LocalDate? = null,
//    val location: String? = null,
//    val address: String? = null,
//    val imageUrl: String? = null,
//    val description: String? = null,
//    val ageMin: Int? = null,
//    val contact: String? = null,
//    val url: String? = null,