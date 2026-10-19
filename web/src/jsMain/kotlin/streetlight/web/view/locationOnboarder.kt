package streetlight.web.view

import kampfire.model.Labeled
import kampfire.model.combine
import kampfire.model.reactIn
import kampfire.model.refine
import kampfire.model.storeOf
import koala.SvgFile
import koala.dom.ViewScope
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds
import koala.dom.component.*
import koala.dom.layout.*
import koala.markdown.lazyMarkdown
import koala.markdown.markdown
import koala.markdown.toMarkdown
import koala.modifier.*
import kotlinx.coroutines.FlowPreview
import kotlinx.css.dvh
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.LocationEdit
import streetlight.model.data.Star
import streetlight.model.ui.LocationScoutRoute
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.layout.BodyStyle
import streetlight.web.model.FinishedStage
import streetlight.web.model.HostedLocations
import streetlight.web.model.LocationOnboarder
import streetlight.web.model.NullLocation
import streetlight.web.model.ParseModeFocus
import streetlight.web.model.getLocationEditor
import streetlight.web.model.getLocationOnboarder
import streetlight.web.model.portal
import streetlight.web.model.route
import streetlight.model.ui.StarDashRoute

context(star: Star)
fun ViewScope.locationOnboarder() {
    val editor = app.getLocationEditor(LocationEdit(), contentScope)
    val model = app.getLocationOnboarder(star.username, editor, contentScope)

    context(model, editor) {
        flowBlock(model.focusState, modify(Magic, MinHeight(60.dvh))) { focus ->
            when (focus) {
                null -> onboardStage(0) {
                    column(modify(AlignItemsCenter, JustifyContentCenter)) {
                        centeredText("Looking for hosted locations...", Italic)
                        icon(SvgFile.Book.animated, Height(10))
                    }
                }
                is HostedLocations -> onboardStage(0) {
                    hostedLocations(focus.locations)
                }
                NullLocation -> onboardStage(0) {
                    locationSearch()
                }
                is LocationEdit -> onboardStage(0) {
                    column(BodyStyle.MainColumn) {
                        locationEditFormBody()
                        locationHostForm()
                        formSubmit("Next", model::submitLocation, editor.messages, Primary)
                    }
                }
                is LocationConfigContent -> onboardStage(1) {
                    designOnboarder()
                }
                is ParseModeFocus -> onboardStage(2) {
                    parseOnboarder(focus)
                }
                is FinishedStage -> onboardStage(3) {
                    finishedStage(focus)
                }
                else -> error("unknown Onboarder focus: $focus")
            }
        }
    }
}

fun ViewScope.onboardStage(stage: Int, content: ViewScope.() -> Unit) {
    column {
        row(modify(Gap(2), Padding(2), JustifyContentCenter, FlexWrap, TextUppercase, TextSmall, TextShadow)) {
            LocationOnboardStage.entries.forEach {
                val mod = when {
                    stage == it.ordinal -> PrimaryFg
                    stage > it.ordinal -> modify(OpacityHalf, Strikethrough)
                    else -> OpacityHigh
                }
                textBlock("${it.ordinal + 1}. ${it.label}", mod)
            }
        }
        content()
    }
}

private enum class LocationOnboardStage(override val label: String): Labeled {
    Create("Create a location"),
    Design("Design your page"),
    Connect("Share your events");
}

/** The end of the onboarding of the [focus] location, leading to its page or the dashboard. */
private fun ViewScope.finishedStage(focus: FinishedStage) = formRow {
    formSection("All Set") {
        centeredText("${focus.location.name ?: "Your location"} is ready on Streetlight.")
        row(FlexItems1) {
            button("go to dashboard", { portal.go(StarDashRoute()) }, Zen)
            button("view location", { portal.go(focus.location.route) }, Primary)
        }
    }
}

/** The [hosted] locations of the star, to pick up where they left off, or to add a new one. */
context(model: LocationOnboarder)
private fun ViewScope.hostedLocations(hosted: List<LocationConfigContent>) = formRow {
    val hostedState = storeOf(hosted)

    formSection("Welcome Back") {
        markdown(welcomeBackInfo)
    }

    formSection("Your Locations") {
        messageBox(model.focusMessage)
        selectorContainer {
            selectorBlock(items = hostedState, onSelect = model::resumeHosted) { content ->
                searchItem(content.location.name, content.location.address, content.location.city)
            }
            button("add a new location", model::addNewLocation, Zen)
        }
    }
}

private val welcomeBackInfo by lazyMarkdown("""
It looks like you are already the host of one or more locations. You can pick up where you left off or start a new configuration. ${SvgFile.Restore.animated.toMarkdown()}
""")

/** The name and address of the owner's location, and the locations found for them. */
@OptIn(FlowPreview::class)
context(model: LocationOnboarder)
private fun ViewScope.locationSearch() = formRow {
    val nameState = storeOf("")
    val addressState = storeOf("")
    val cityState = storeOf("")
    nameState.combine(addressState) { name, address -> name to address }
        .combine(cityState) { (name, address), city -> Triple(name, address, city) }
        .refine { it.debounce(500.milliseconds) }
        .reactIn(contentScope) { (name, address, city) -> model.searchLocations(name, address, city) }
    val canSearchOSMState = nameState.combine(model.isOSMState) { name, isOSM -> name.isNotBlank() && !isOSM }

    formSection("Location Search") {
        textField(nameState, "name")
        row {
            textField(addressState, "address", Flex2)
            textField(cityState, "city", Flex1)
        }
        messageBox(model.searchMessage)
        selectorContainer {
            selectorBlock(items = model.resultsState, onSelect = model::selectLocation) { location ->
                searchItem(location.name, location.address, location.city)
            }
            flowBlock(canSearchOSMState) { canSearchOSM ->
                if (canSearchOSM) {
                    button(
                        "Not listed? Search OpenStreetMap",
                        { model.searchOSM(nameState.now, addressState.now, cityState.now) },
                        modify(Zen, AlignSelfCenter)
                    )
                }
            }
        }
    }

    formSection("How it works") {
        markdown(locationSearchIntro)
    }
}

// language="MD"
val locationSearchIntro by lazyMarkdown("""
This tool helps you get started with a location you own or have permission to manage. [Click here](${LocationScoutRoute(null).toRelativePath()}) share a location that you are not affiliated with. 

* Start typing in any of the fields to search our location database. 
* If you don't see your location, click the **Search OpenStreetMap** button. 
* This tool helps us pinpoint the geolocation so we can add it to the map.
""")