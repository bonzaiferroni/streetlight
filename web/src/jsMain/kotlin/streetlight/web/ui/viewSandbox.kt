package streetlight.web.ui

import kampfire.model.LiveList
import koala.css.AlignItemsCenter
import koala.css.AlignItemsStart
import koala.css.Flex1
import koala.css.Flex3
import koala.css.Height100Pct
import koala.css.Height100Vh
import koala.css.Padding1
import koala.css.Zen
import koala.css.modify
import koala.dom.*
import kotlinx.coroutines.delay
import web.dom.ElementId
import web.dom.document
import web.scroll.ScrollBehavior
import web.scroll.ScrollToOptions
import web.scroll.smooth
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

fun RouteScope.viewSandbox() {
    // val model = Sandbox(contentScope, api, toaster)

    console.log("welcome to sandbox")

    val list = LiveList((0..10000).map { Uuid.random() to it }) { it.first }

    row(modify(AlignItemsStart, Height100Vh)) {
        val element = lazyColumn(list, modify(Flex1, Height100Pct)) { item ->
            row(modify(Padding1, AlignItemsCenter)) {
                textBlock("Foo ${item.second}", modify(Flex1))
                button("insert", {
                    launchEffect {
                        list.insertAfter(item.first, Uuid.random() to 67)
                    }
                }, modify(Zen))
                button("remove", {
                    launchEffect {
                        list.remove(item.first)
                    }
                }, modify(Zen))
                button("replace", {
                    launchEffect {
                        list.replace(item.first to item.second + 1)
                    }
                }, modify(Zen))
            }
        }
        column(modify(Flex1, Padding1)) {
            row {
                button("clear", {
                    launchEffect {
                        list.clear()
                    }
                })
                button("remove 10", {
                    launchEffect {
                        list.removeAt(0, 10)
                    }
                })
                button("add 1", {
                    launchEffect {
                        list.insertAt(0, Uuid.random() to 0)
                    }
                })
            }
        }

        launchEffect {
            repeat(10) {
                delay(1.seconds)
                element.scrollBy(ScrollToOptions(top = 1000.0, behavior = ScrollBehavior.smooth))
            }
            document.body.append(document.createElement("div").apply { id = ElementId("scroll-done") })
        }
    }
}

class SandboxException : Exception("Arrr sandbox exception")

