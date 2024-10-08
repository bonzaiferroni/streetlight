package streetlight.web.ui.pages

import io.kvision.core.AlignItems
import io.kvision.core.Container
import io.kvision.core.Transition
import io.kvision.core.onClickLaunch
import io.kvision.form.text.text
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.p
import io.kvision.panel.FlexPanel
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.state.bindTo
import streetlight.model.core.Area
import streetlight.web.Layout
import streetlight.web.core.PortalEvents
import streetlight.web.gap
import streetlight.web.io.stores.AreaStore
import streetlight.web.ui.components.col
import streetlight.web.ui.components.row

fun Container.homePage(): PortalEvents? {
    val message = ObservableValue("hello, world!")

    col {
        val p = p {
            transition = Transition("all", .3, "ease")
        }
        p.bind(message) {
            +it
        }
        button("show") {
            onClick {
                if (p.opacity != 0.0) {
                    p.opacity = 0.0
                } else {
                    p.opacity = 1.0
                }
            }
        }
        text() {
            placeholder = "Enter your message"
        }.bindTo(message)

        areas()
    }

    return null
}

fun Container.areas() {
    val store = AreaStore()
    lateinit var panel: FlexPanel
    val areaName = ObservableValue("")

    col {
        button("get").onClickLaunch {
            panel.refreshAreas(store)
        }
        row(alignItems = AlignItems.CENTER) {
            gap = Layout.defaultGap
            text() {
                placeholder = "area"
            }.bindTo(areaName)
            val createButton = button("create")
            createButton.onClickLaunch {
                val id = store.create(Area(name = areaName.value))
                createButton.text = "created $id"
                panel.refreshAreas(store)
            }
        }
        panel = col()
    }
}

suspend fun Container.refreshAreas(store: AreaStore) {

    this.removeAll()
    val areas = store.getAll()
    suspend fun refresh() { refreshAreas(store) }
    areas.forEach { area ->
        val areaName = ObservableValue(area.name)
        row(alignItems = AlignItems.CENTER) {
            text().bindTo(areaName)
            button("", icon = "fas fa-trash", style = ButtonStyle.DANGER).onClickLaunch {
                store.delete(area.id)
                refresh()
            }
            button("", icon = "fas fa-refresh", style = ButtonStyle.PRIMARY).onClickLaunch {
                console.log(areaName.value)
                store.update(area.copy(name = areaName.value))
            }
        }
    }
}