package streetlight.web.ui

import kampfire.api.isValid
import kampfire.api.toSlug
import kampfire.model.CoreProblem
import kampfire.model.handleResponse
import koala.dom.*
import koala.html.div
import koala.model.MutableField
import koala.model.mutableFieldOf
import streetlight.model.data.LocationConfig
import streetlight.model.data.SubdomainConfig

fun ViewScope.locationSettingsForm(configState: MutableField<LocationConfig>) = formColumn {
    formRow {
        subdomainSection(configState)
        div { }
    }
}

fun ViewScope.subdomainSection(configState: MutableField<LocationConfig>) = formSection("subdomain") {
    val subdomainState = configState.mutableFieldOf({ it.subdomain?.value ?: "" }) { copy(subdomain = it.toSlug())}
    val messages = MessageStore()

    formText("A subdomain provides easy access to your Streetlight page.")
    textField(subdomainState)
    flowBlock(subdomainState) {
        textBlock("currently: $it.streetlight.ing")
    }
    formSubmit("save", {
         val slug = subdomainState.now.takeIf { it.isNotBlank() }?.toSlug() ?: return@formSubmit
         if (!slug.isValid()) {
             messages.deliver(CoreProblem.InvalidSlug)
             return@formSubmit
         }
         val locationId = configState.now.locationId
        launchEffect {
            messages.deliverSending()
            api.configSubdomain(SubdomainConfig(locationId, slug)).handleResponse(messages, "Subdomain is active.") {
                configState.set { copy(subdomain = slug) }
            }
        }
    }, messages)
}