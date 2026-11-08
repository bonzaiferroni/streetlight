package streetlight.web.model

import kampfire.api.Username
import koala.dom.AppContainer
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Account
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.LocationEdit
import streetlight.model.data.MediaEdit
import streetlight.model.data.StarEdit

// the factories of the Dash section's view models, kept apart from webModule so they load with its chunk

fun AppContainer.getLocationEditor(edit: LocationEdit, scope: CoroutineScope) =
    LocationEditor(edit, scope, koin.get())

fun AppContainer.getMediaEditor(edit: MediaEdit, scope: CoroutineScope) =
    MediaEditor(edit, scope, koin.get(), koin.get())

fun AppContainer.getLocationScout(galaxy: Galaxy?, editor: LocationEditor, scope: CoroutineScope) =
    LocationScout(galaxy, editor, scope, koin.get(), koin.get(), koin.get(), koin.get(), koin.get())

fun AppContainer.getEventEditor(edit: EventEdit, scope: CoroutineScope) =
    EventEditor(edit, scope, koin.get())

fun AppContainer.getEventScout(galaxy: Galaxy?, editor: EventEditor, location: LocationScout, scope: CoroutineScope) =
    EventScout(galaxy, editor, location, scope, koin.get(), koin.get(), koin.get())

fun AppContainer.getGalaxyEditor(galaxy: GalaxyEdit, scope: CoroutineScope) =
    GalaxyEditor(galaxy, scope, koin.get(), koin.get(), koin.get(), koin.get())

fun AppContainer.getSiteMonitor(scope: CoroutineScope) = SiteMonitor(scope, koin.get(), koin.get())

fun AppContainer.getAccountEditor(edit: Account, scope: CoroutineScope) =
    AccountEditor(edit, scope, koin.get(), koin.get(), koin.get())

fun AppContainer.getProfileEditor(edit: StarEdit, scope: CoroutineScope) =
    ProfileEditor(edit, scope, koin.get(), koin.get())

fun AppContainer.getLocationOnboarder(username: Username, editor: LocationEditor, scope: CoroutineScope) =
    LocationOnboarder(username, editor, scope, koin.get(), koin.get())
