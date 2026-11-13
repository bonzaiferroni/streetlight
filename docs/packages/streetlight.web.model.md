# Package streetlight.web.model

## Introduction

View models, each holding the state behind one view and the calls that change it, and the app's services, in `jsMain`. Models the server also renders from, such as the entity properties, `FlairIcon` and the CSS of `EntityType`, live in `commonMain`.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model` | DTOs and routes |
| `streetlight.model.io` | `ApiClient` |
| `kampfire.model` | `Store`, `Tap`, `Messenger` and `Outcome` |
| `koala.model` | `Portal` and `GeoCamera` |
| `kotlinx.coroutines` | View model work |

## Services

`webModule` is the Koin module holding every browser-side service. `AppContainer` wraps the resulting Koin instance and is reached through `ViewScope.app`.

A view model with per-view state is not registered in the module. It is built by a factory extension on `AppContainer` that takes the scope and any starting value, so each view gets its own.

`webModule` joins the main bundle, and so does everything it constructs. A service or factory used only by a lazily loaded section lives in that section's chunk: its services in a `fooModule` its portal loads into Koin on first use, as `studioModule`, and its factories in a file of their own, as `dashFactories.kt`.

The studio's records live in `StudioIdbDb`, the `StudioDb` over IndexedDB, with a table per record and a `FooIdbDao` over each. Audio stays on the device whether or not the user has an account; the server only knows where a shared clip lives.

A take's downbeat sits `leadSeconds` into its recording: the time from the capture's start to the scheduled downbeat, plus the round-trip latency, since the player hears the count late and their sound arrives late again. Playback then needs no compensation, as clips and generated tones leave by the same path. A take plays in place of its section's tones while its tempo, time, key and bars still fit the mix, and the tones return once they don't.

A clip's audio is encoded once, as mono Opus at 48 kHz and 96 kbps, beyond which quality gains don't matter to a playground. Every later change to a clip, such as its trim or an effect, stays in its metadata, so the only second encode is a deliberate one, as a mix's export. Stereo returns at mix time.

A form model, named `FooForm`, holds the properties of a record apart from it, seeded from the record, until a save reads them back. It is what an edit popover binds its fields to, so the record changes once, on save. A form that would carry no functions is not a model: its popover declares the stores itself with `storeOf`.

A section's text is its notation alone. Its name, its properties and the arrangements that play it are set through the UI, and a section is created by an action that names it: a name the music holds joins that section, and any other gives the music a new one.

A music sits upstream of what is built on it and knows nothing of it. `MusicEditor` holds no mix; `MixEditor` reads it. A track removed takes its section clips and studies with it when the removal is saved, and what still names it, such as a mix's hidden sections or a focus, skips a track the music no longer holds. A rename needs no following, since a track keeps its id.

`MusicEditor` holds its `MusicSheet` as an edit buffer, saved whole once edits pause for a second and once more as its scope ends. Every other studio edit, such as a mix's, saves at once and is seen back through the DAO's flows. A change made from what is shown reads the record again first, so changes in quick succession all land.

## Entity Properties

A property that depends on the entity type is an `Entity` extension function in `EntityUtility.kt`, resolved by a `when` over every entity type. A new `Entity` type adds a branch to each.

| Form | Used when | Example |
|---|---|---|
| `toFoo()` extension function | The entity alone decides the value | `Entity.toButtons()` |
| `fooOf(entity, …)` function | The value also depends on where it is shown | |

A property returns `null` for content the entity does not have, and a parameter that receives it is nullable.

## Construction

A view model takes its collaborators through its constructor. The view builds it, so the model never reaches into the container itself.

```kotlin
val model = FeedbackHub(contentScope, api, toaster)
```

A model needing more than a handful of services gets a factory extension on `AppContainer`, which supplies the container's half and takes the view's half as parameters.

```kotlin
fun AppContainer.getGalaxyEditor(galaxy: GalaxyEdit, scope: CoroutineScope) =
    GalaxyEditor(galaxy, scope, koin.get(), koin.get(), koin.get(), koin.get())
```

`RouteDock` is an app-wide model, registered in `webModule` and documented in `streetlight.web.view.md`.

## Scope

A view model takes the view's `contentScope`. Its coroutines then end when the content is replaced, so a request outlives neither the view that wanted it nor the page it was drawn on.

## State

A model holds one `Store` of a single state class, and exposes lenses onto it.

| Exposure | Built with | For |
|---|---|---|
| A read | `tapOf` | Content a view renders |
| A write | `mutableTapOf` | A field a control edits |

A control binds to the narrowest lens that covers it, so editing one field leaves the rest of the state untouched.

A lens is named for its value with the suffix `State`, read or write alike, as `nameState` and `focusState`. A function returning one, such as the lens of one tag among many, follows the same rule, as `tagState(tag)`.

### Route Filters

A filter a route carries, such as the tag of `EventsMapRoute`, is read from the route as a lens on `Portal.routeState`. A control that sets it calls a model method, which navigates with the new value. A filter change replaces the current route rather than appending to the backstack.

A filter edited faster than the route, such as search text behind a debounce, keeps its own `Store`, seeded from the current route. The model navigates when the debounced value differs from the route's. It copies a route's value into the store only when the route is not the one it last issued, as on a back navigation, so a route that lands while the viewer types does not overwrite the field.

## Messages

A method that reports to the user takes a `Messenger` parameter. The view decides where the message appears, the model decides what it says. Without an obvious place in the view for the message, the model reports through its injected `Toaster` instead. `streetlight.web.view` states the conventions the messages follow.

## Editing

A `FooEditor` takes its initial value, usually a `FooEdit`, and its whole role is to manage that state and submit it. Whatever builds it decides the initial value, such as a route block or a config model's staged edit, and the editor takes it from there; it does not choose which record to edit or load it.

A model editing a record holds an edit DTO in its state. On a successful send it clears the fields the sender filled and keeps the choices they made, so a second send starts from the same settings.

A field the record's DTO does not carry, such as `MediaEdit.skillIds`, is nullable on the edit DTO: `null` leaves the stored value alone. The editor loads it in its `init` with its own API call and writes it into the edit only while the field is still `null`, so a choice made during the load wins. `SkillLinkEditor` does this for `skillIds`, given the edit's field and the read of the record's links.

## Submodels

A submodel is a model a base model declares as a public property, built in its own constructor, holding its own state. It is named with the suffix `Editor`. It carries functionality two models share, such as `EmailEditor` and `LeadEditor`, or one concern of a large model, such as `MusicPropertyEditor` in `MusicEditor`. The view reaches it through the model. The submodel holds its own state, publishes it, and makes its own API call, so the base models stay clean and the work is written once. What differs between its users, such as a `LeadEditor`'s `LeadType`, is passed through its constructor.

## Images

An `ImageEditor` takes the `ImageTarget` of the record it edits, and uploads at that target's sizes. An image a model uploads without one, such as a layout block's, names its target in the `uploadImage` call.

## Viewer Settings

A setting the viewer keeps across sessions is a field of `SiteConfigState`, exposed as a lens on `SiteConfig`. A model that follows the setting takes `SiteConfig` from the container and declares the lens as its own property, such as `postModeState`, so its view binds to the model.

## Posting

A scout's `galaxy` is optional. Without one, `post()` saves the record and creates no post row, and the record is found through its city.

A scout reports a successful post to the `Toaster`, since the view it posted from may be gone. Its post mode, `LocationPostMode` or `EventPostMode`, decides what follows.

| Mode | After the post |
|---|---|
| `ResetLocation` | `reset()`, back to the location search |
| `ResetEvent` | `resetEvent()`, back to the event search at the same location |
| `Return` | Sets `isPosted`, which the view follows to the galaxy, or to `HomeRoute` when there is no galaxy |

## Models

### Rendered by the Server

| Model | Role |
|---|---|
| [`toButtons`, `toFlair`…](../../web/src/commonMain/kotlin/streetlight/web/model/EntityUtility.kt) | The entity properties, as specified under Entity Properties, and the custom property and rgb of each `EntityType`'s color |
| [`FlairIcon`](../../web/src/commonMain/kotlin/streetlight/web/model/FlairIcon.kt) | The icon badge of an entity type |
| [`SiteDoc`](../../web/src/commonMain/kotlin/streetlight/web/model/SiteDoc.kt) | The site's own docs, such as About and Privacy |

### Services

| Model | Role |
|---|---|
| [`webModule`](../../web/src/jsMain/kotlin/streetlight/web/model/webModule.kt) | The web client's services, and the factories of per-view models |
| [`AppFacade.api, AppFacade.portal…`](../../web/src/jsMain/kotlin/streetlight/web/model/StreetlightFacade.kt) | Services reached from any `AppFacade` |
| [`SessionClient`](../../web/src/jsMain/kotlin/streetlight/web/model/SessionClient.kt) | The signed-in star, and sign-in and sign-out |
| [`AppContentFetcher`](../../web/src/jsMain/kotlin/streetlight/web/model/AppContentFetcher.kt) | The content of each route |
| [`RouteDock`](../../web/src/jsMain/kotlin/streetlight/web/model/RouteDock.kt) | The state of the route dock |
| [`SiteConfig`](../../web/src/jsMain/kotlin/streetlight/web/model/SiteConfig.kt) | The viewer's site settings |
| [`DataCache`](../../web/src/jsMain/kotlin/streetlight/web/model/DataCache.kt) | Data loaded once per session |
| [`StarCache`](../../web/src/jsMain/kotlin/streetlight/web/model/StarCache.kt) | The items a star has lit |
| [`Fleet`](../../web/src/jsMain/kotlin/streetlight/web/model/Fleet.kt) | A list loaded on first request |
| [`songTonesOf`](../../web/src/jsMain/kotlin/streetlight/web/model/SongTones.kt) | The tones of a song's tracks laid along its arrangement, which playback and export share |
| [`writeMidi`](../../web/src/jsMain/kotlin/streetlight/web/model/writeMidi.kt) | A song's tones as a Standard MIDI File, and its export as a download |
| [`StudioAudio`](../../web/src/jsMain/kotlin/streetlight/web/model/StudioAudio.kt) | The audio context the studio shares, resumed as recording or playback starts, with the recorder and PCM player on it, its calibration and latency |
| [`DrumKit`](../../web/src/jsMain/kotlin/streetlight/web/model/DrumKit.kt) | The synthesized drums `ToneSynth` strikes |
| [`ToneSynth`](../../web/src/jsMain/kotlin/streetlight/web/model/ToneSynth.kt) | The synthesized tones and drums, sounded into a live or offline context |
| [`TonePlayer`](../../web/src/jsMain/kotlin/streetlight/web/model/TonePlayer.kt) | Plays notes and clicks live, apart from any song |
| [`TrackPlayer`](../../web/src/jsMain/kotlin/streetlight/web/model/TrackPlayer.kt) | Plays the tracks of a song through its arrangement with synthesized tones, or renders them offline for an export |
| [`ClipLibrary`](../../web/src/jsMain/kotlin/streetlight/web/model/ClipLibrary.kt) | The clips a user plays, kept on the device with or without an account: each record in the local database, its audio as an Ogg Opus file in the browser's private file system |
| [`OpusCodec`](../../web/src/jsMain/kotlin/streetlight/web/model/OpusCodec.kt), [`OggOpus`](../../web/src/jsMain/kotlin/streetlight/web/model/OggOpus.kt) | A clip's one encode to Ogg Opus through WebCodecs, and its decode |
| [`readDeviceAgent`](../../web/src/jsMain/kotlin/streetlight/web/model/DeviceAgent.kt) | The browser described for a bug report |

### Editors

| Model | Role |
|---|---|
| [`AccountEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/AccountEditor.kt) | Edits the signed-in account |
| [`ProfileEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/ProfileEditor.kt) | Edits the star's profile and its design |
| [`UserCreator`](../../web/src/jsMain/kotlin/streetlight/web/model/UserCreator.kt) | Creates an account and signs in |
| [`CredentialStore`](../../web/src/jsMain/kotlin/streetlight/web/model/CredentialStore.kt) | The sign-in form |
| [`EmailEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/EmailEditor.kt) | An email field |
| [`PasswordEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/PasswordEditor.kt) | A password field with its confirmation |
| [`AppConnector`](../../web/src/jsMain/kotlin/streetlight/web/model/AppConnector.kt) | The payment handles a star links to their profile |
| [`SellerConnector`](../../web/src/jsMain/kotlin/streetlight/web/model/SellerConnector.kt) | The star's seller account and its onboarding |
| [`SellerConfig`](../../web/src/jsMain/kotlin/streetlight/web/model/SellerConfig.kt) | The star's offerings, payments and customers, and the edit staged as the panel's focus |
| [`OfferingEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/OfferingEditor.kt) | Creates or updates an offering |
| [`PaymentEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/PaymentEditor.kt) | Creates a payment for an offering |
| [`CityEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/CityEditor.kt) | Edits a city |
| [`EventEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/EventEditor.kt) | Creates or updates an event |
| [`LocationEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/LocationEditor.kt) | Creates or updates a location |
| [`MediaEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/MediaEditor.kt) | Creates or updates media |
| [`GalaxyEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/GalaxyEditor.kt) | Creates or updates a galaxy |
| [`ImageEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/ImageEditor.kt) | Uploads a picked image |
| [`AudioEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/AudioEditor.kt) | Uploads the picked audio of an audio post |
| [`DesignEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/DesignEditor.kt) | Edits the layout and theme of a page |
| [`LayoutEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/LayoutEditor.kt) | Edits a page layout as a tree of blocks and containers |
| [`ContainerEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/ContainerEditor.kt) | One container of a layout |
| [`BlockEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/BlockEditor.kt) | One block of a layout |
| [`buildBlock`](../../web/src/jsMain/kotlin/streetlight/web/model/buildBlock.kt) | Rebuilds a block from its edited containers |
| [`ThemeEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/ThemeEditor.kt) | Edits a page theme |
| [`LeadEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/LeadEditor.kt) | A url sent for the crawler to read |

### Screens

| Model | Role |
|---|---|
| [`EventScout`](../../web/src/jsMain/kotlin/streetlight/web/model/EventScout.kt) | Posts an event in stages |
| [`LocationScout`](../../web/src/jsMain/kotlin/streetlight/web/model/LocationScout.kt) | Finds or creates a location and posts it |
| [`FeedbackHub`](../../web/src/jsMain/kotlin/streetlight/web/model/FeedbackHub.kt) | Lists and sends feedback |
| [`BugReporter`](../../web/src/jsMain/kotlin/streetlight/web/model/BugReporter.kt) | Sends a bug report |
| [`Inbox`](../../web/src/jsMain/kotlin/streetlight/web/model/Inbox.kt) | A star's chats and messages |
| [`ChatRoom`](../../web/src/jsMain/kotlin/streetlight/web/model/ChatRoom.kt) | A live chat over a socket |
| [`MusicianHub`](../../web/src/jsMain/kotlin/streetlight/web/model/MusicianHub.kt) | Lists and adds songs |
| [`SongProfile`](../../web/src/jsMain/kotlin/streetlight/web/model/SongProfile.kt) | Shows and edits a song |
| [`MusicStudio`](../../web/src/jsMain/kotlin/streetlight/web/model/MusicStudio.kt) | Lists, creates and removes the musics in `StudioDb`, and reads the sheet of the one in focus for its `MusicEditor` and `MixEditor` |
| [`MixEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/MixEditor.kt) | Edits the mixes of a song's music, whose tempo and takes playback follows |
| [`MixForm`](../../web/src/jsMain/kotlin/streetlight/web/model/MixForm.kt) | Holds a mix's name, tempo and sounding key apart from the mix until `MixEditor` applies them |
| [`MusicEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/MusicEditor.kt) | Edits the music and tracks of the music in focus in `MusicStudio` |
| [`SectionEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/SectionEditor.kt) | Edits one section of a track through the text of its notation |
| [`MusicForm`](../../web/src/jsMain/kotlin/streetlight/web/model/MusicForm.kt) | Holds a music's key, time, tempo and capo apart from the music until they are applied |
| [`LaneEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/LaneEditor.kt) | Lays a song's tracks out as lanes of sections on a timeline, and plays them from its cursor |
| [`NoteDrag`](../../web/src/jsMain/kotlin/streetlight/web/model/NoteDrag.kt) | A note dragged in a lane, and the snapping and edit it makes |
| [`NotationField`](../../web/src/jsMain/kotlin/streetlight/web/model/NotationField.kt) | The text of a notation and the problems found reading it, which the notation editors share |
| [`StudioIdbDb`](../../web/src/jsMain/kotlin/streetlight/web/model/StudioIdbDb.kt) | The `StudioDb` kept in the browser, and its DAOs |
| [`SectionClipEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/SectionClipEditor.kt) | Chooses the clip a mix binds to a track's section among those that fit it |
| [`SectionMic`](../../web/src/jsMain/kotlin/streetlight/web/model/SectionMic.kt) | Records a take of a track's section after a counted lead-in, keeping it only when it reaches the end |
| [`TakeShelf`](../../web/src/jsMain/kotlin/streetlight/web/model/SongTakes.kt) | Reads the clips of a mix that still fit its song, decoded for playback |
| [`TimelineModel`](../../web/src/jsMain/kotlin/streetlight/web/model/TimelineModel.kt) | The state a timeline shows, which `StudioEditor` and `LaneEditor` share |
| [`StudioEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/StudioEditor.kt) | Records and plays samples on a timeline |
| [`SiteMonitor`](../../web/src/jsMain/kotlin/streetlight/web/model/SiteMonitor.kt) | Charts the site's status |
| [`Sandbox`](../../web/src/jsMain/kotlin/streetlight/web/model/Sandbox.kt) | The state of the sandbox |
| [`LocationOnboarder`](../../web/src/jsMain/kotlin/streetlight/web/model/LocationOnboarder.kt) | Finds a location owner's location in Streetlight or OpenStreetMap |

### Map

| Model | Role |
|---|---|
| [`Earth`](../../web/src/jsMain/kotlin/streetlight/web/model/Earth.kt) | The model of the map screens |
| [`EarthCache`](../../web/src/jsMain/kotlin/streetlight/web/model/EarthCache.kt) | The events of each settled map view |
| [`setPoints`, `showStarLocation`…](../../web/src/jsMain/kotlin/streetlight/web/model/markerMapPoints.kt) | Sets a `MarkerMap`'s markers from entities and shows the viewer's location on it |
| [`StreetMarker`](../../web/src/jsMain/kotlin/streetlight/web/model/StreetMarker.kt) | The markers of entities |
| [`InflateMarker`](../../web/src/jsMain/kotlin/streetlight/web/model/InflateMarker.kt) | The markers of event groups |
| [`StarMarker`](../../web/src/jsMain/kotlin/streetlight/web/model/StarMarker.kt) | The viewer's own location marker |
| [`PrototypeMarker`](../../web/src/jsMain/kotlin/streetlight/web/model/PrototypeMarker.kt) | The marker id of a spirit |
| [`MapTypeIcon`](../../web/src/jsMain/kotlin/streetlight/web/model/MapTypeIcon.kt) | The icon of a location marker by its map type |
| [`StarLocator`](../../web/src/jsMain/kotlin/streetlight/web/model/StarLocator.kt) | The viewer's location |
| [`SpiritMap`](../../web/src/jsMain/kotlin/streetlight/web/model/SpiritMap.kt) | The map's position shared with other viewers |
| [`TransitMap`](../../web/src/jsMain/kotlin/streetlight/web/model/TransitMap.kt) | Transit routes and vehicle positions |
