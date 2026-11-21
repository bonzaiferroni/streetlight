# Package streetlight.web.model

View models, each holding the state behind one view and the calls that change it, and the app's services, in `jsMain`. Models the server also renders from, such as the entity properties, `FlairIcon` and the CSS of `EntityType`, live in `commonMain`.

## Style

| Document | Covers |
|---|---|
| [`mvvm-style.md`](../../../../../../../docs/mvvm-style.md) | A guide to defining viewmodels and their state. |

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

A notation's text is its measures alone. Its name, the sections it plays and the arrangements that hold it are set through the UI. A section belongs to the music and is created there with its measures. A notation is created by an action that names it, and plays the section of its name when the track plays nothing there.

A music sits upstream of what is built on it and knows nothing of it. `MusicEditor` holds no clips; `ClipEditor` and `ClipMic` read its sheet. A track removed takes its track clips and studies with it when the removal is saved, and what still names it, such as a focus, skips a track the music no longer holds. A rename needs no following, since a track keeps its id.

Each record of a music has one writer at a time. `MusicEditor` holds the music as an edit buffer, saved once edits pause for a second and once more as its scope ends, and its tracks follow the DAO's flows. The editor of a focus owns its track the same way: a `TrackEditor` or `NotationEditor` is seeded with the track its `MusicEditorFocus` holds, saves it as `MusicEditor` saves the music, and hands the track as it stands to the focus that follows, so the next editor never seeds from a save still landing. With no focus, `MusicEditor` writes a track directly, one save per edit. Every other studio edit, such as a clip binding, saves at once and is seen back through the DAO's flows.

## Entity Properties

A property that depends on the entity type is an `Entity` extension function in `EntityUtility.kt`, resolved by a `when` over every entity type. A new `Entity` type adds a branch to each.

| Form | Used when | Example |
|---|---|---|
| `toFoo()` extension function | The entity alone decides the value | `Entity.toButtons()` |
| `fooOf(entity, …)` function | The value also depends on where it is shown | |

A property returns `null` for content the entity does not have, and a parameter that receives it is nullable.

```kotlin
val model = FeedbackHub(contentScope, api, toaster)
```

A model needing more than a handful of services gets a factory extension on `AppContainer`, which supplies the container's half and takes the view's half as parameters.

```kotlin
fun AppContainer.getGalaxyEditor(galaxy: GalaxyEdit, scope: CoroutineScope) =
    GalaxyEditor(galaxy, scope, koin.get(), koin.get(), koin.get(), koin.get())
```

`RouteDock` is an app-wide model, registered in `webModule` and documented in [`streetlight.web.view`](../../../../../commonMain/kotlin/streetlight/web/view/README.md).

### Route Filters

A filter a route carries, such as the tag of `EventsMapRoute`, is read from the route as a lens on `Portal.routeState`. A control that sets it calls a model method, which navigates with the new value. A filter change replaces the current route rather than appending to the backstack.

A filter edited faster than the route, such as search text behind a debounce, keeps its own `Store`, seeded from the current route. The model navigates when the debounced value differs from the route's. It copies a route's value into the store only when the route is not the one it last issued, as on a back navigation, so a route that lands while the viewer types does not overwrite the field.

## Editing

A `FooEditor` takes its initial value, usually a `FooEdit`, and its whole role is to manage that state and submit it. Whatever builds it decides the initial value, such as a route block or a config model's staged edit, and the editor takes it from there; it does not choose which record to edit or load it.

A model editing a record holds an edit DTO in its state. On a successful send it clears the fields the sender filled and keeps the choices they made, so a second send starts from the same settings.

A field the record's DTO does not carry, such as `MediaEdit.skillIds`, is nullable on the edit DTO: `null` leaves the stored value alone. The editor loads it in its `init` with its own API call and writes it into the edit only while the field is still `null`, so a choice made during the load wins. `SkillLinkEditor` does this for `skillIds`, given the edit's field and the read of the record's links.

## Images

An `ImageEditor` takes the `ImageTarget` of the record it edits, and uploads at that target's sizes. An image a model uploads without one, such as a layout block's, names its target in the `uploadImage` call.

## Viewer Settings

A setting the viewer keeps across sessions is a field of `SiteConfigState`, exposed as a lens on `SiteConfig`. A model that follows the setting takes `SiteConfig` from the container and declares the lens as its own property, such as `postModeState`, so its view binds to the model.

The studio's settings are held by `MusicConfig`, which a screen builds and provides to its views as a context parameter. A model never takes `MusicConfig`; the view passes the state a model needs, such as `notationStyleState`, as an argument.

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
| [`toButtons`, `toFlair`…](EntityUtility.kt) | The entity properties, as specified under Entity Properties, and the custom property and rgb of each `EntityType`'s color |
| [`FlairIcon`](FlairIcon.kt) | The icon badge of an entity type |
| [`SiteDoc`](SiteDoc.kt) | The site's own docs, such as About and Privacy |

### Services

| Model | Role |
|---|---|
| [`webModule`](webModule.kt) | The web client's services, and the factories of per-view models |
| [`AppFacade.api, AppFacade.portal…`](StreetlightFacade.kt) | Services reached from any `AppFacade` |
| [`SessionClient`](SessionClient.kt) | The signed-in star, and sign-in and sign-out |
| [`AppContentFetcher`](AppContentFetcher.kt) | The content of each route |
| [`RouteDock`](RouteDock.kt) | The state of the route dock |
| [`SiteConfig`](SiteConfig.kt) | The viewer's site settings |
| [`MusicConfig`](MusicConfig.kt) | The viewer's studio settings: notation style, notation view and whether lyrics show |
| [`DataCache`](DataCache.kt) | Data loaded once per session |
| [`StarCache`](StarCache.kt) | The items a star has lit |
| [`Fleet`](Fleet.kt) | A list loaded on first request |
| [`songTonesOf`](SongTones.kt) | The tones of a song's tracks laid along its arrangement, which playback and export share |
| [`writeMidi`](writeMidi.kt) | A song's tones as a Standard MIDI File, and its export as a download |
| [`StudioAudio`](StudioAudio.kt) | The audio context the studio shares, resumed as recording or playback starts, with the recorder and PCM player on it, its calibration and latency |
| [`DrumKit`](DrumKit.kt) | The synthesized drums `ToneSynth` strikes |
| [`ToneSynth`](ToneSynth.kt) | The synthesized tones and drums, sounded into a live or offline context |
| [`TonePlayer`](TonePlayer.kt) | Plays notes and clicks live, apart from any song |
| [`TrackPlayer`](TrackPlayer.kt) | Plays the tracks of a song through its arrangement with synthesized tones, or renders them offline for an export |
| [`ClipLibrary`](ClipLibrary.kt) | The clips a user plays, kept on the device with or without an account: each record in the local database, its audio as an Ogg Opus file in the browser's private file system |
| [`OpusCodec`](OpusCodec.kt), [`OggOpus`](OggOpus.kt) | A clip's one encode to Ogg Opus through WebCodecs, and its decode |
| [`readDeviceAgent`](DeviceAgent.kt) | The browser described for a bug report |

### Editors

| Model | Role |
|---|---|
| [`AccountEditor`](AccountEditor.kt) | Edits the signed-in account |
| [`ProfileEditor`](ProfileEditor.kt) | Edits the star's profile and its design |
| [`UserCreator`](UserCreator.kt) | Creates an account and signs in |
| [`CredentialStore`](CredentialStore.kt) | The sign-in form |
| [`EmailEditor`](EmailEditor.kt) | An email field |
| [`PasswordEditor`](PasswordEditor.kt) | A password field with its confirmation |
| [`AppConnector`](AppConnector.kt) | The payment handles a star links to their profile |
| [`SellerConnector`](SellerConnector.kt) | The star's seller account and its onboarding |
| [`Merchant`](Merchant.kt) | The star's offerings, payments and customers, and the edit staged as the panel's focus |
| [`OfferingEditor`](OfferingEditor.kt) | Creates or updates an offering |
| [`PaymentEditor`](PaymentEditor.kt) | Creates a payment for an offering |
| [`CityEditor`](CityEditor.kt) | Edits a city |
| [`EventEditor`](EventEditor.kt) | Creates or updates an event |
| [`LocationEditor`](LocationEditor.kt) | Creates or updates a location |
| [`MediaEditor`](MediaEditor.kt) | Creates or updates media |
| [`GalaxyEditor`](GalaxyEditor.kt) | Creates or updates a galaxy |
| [`ImageEditor`](ImageEditor.kt) | Uploads a picked image |
| [`AudioEditor`](AudioEditor.kt) | Uploads the picked audio of an audio post |
| [`DesignEditor`](DesignEditor.kt) | Edits the layout and theme of a page |
| [`LayoutEditor`](LayoutEditor.kt) | Edits a page layout as a tree of blocks and containers |
| [`ContainerEditor`](ContainerEditor.kt) | One container of a layout |
| [`BlockEditor`](BlockEditor.kt) | One block of a layout |
| [`buildBlock`](buildBlock.kt) | Rebuilds a block from its edited containers |
| [`ThemeEditor`](ThemeEditor.kt) | Edits a page theme |
| [`LeadEditor`](LeadEditor.kt) | A url sent for the crawler to read |

### Screens

| Model | Role |
|---|---|
| [`EventScout`](EventScout.kt) | Posts an event in stages |
| [`LocationScout`](LocationScout.kt) | Finds or creates a location and posts it |
| [`FeedbackHub`](FeedbackHub.kt) | Lists and sends feedback |
| [`BugReporter`](BugReporter.kt) | Sends a bug report |
| [`Inbox`](Inbox.kt) | A star's chats and messages |
| [`ChatRoom`](ChatRoom.kt) | A live chat over a socket |
| [`MusicianHub`](MusicianHub.kt) | Lists and adds songs |
| [`SongProfile`](SongProfile.kt) | Shows and edits a song |
| [`MusicStudio`](MusicStudio.kt) | Lists, creates and removes the musics in `StudioDb`, and reads the sheet of the one in focus for its `MusicEditor` |
| [`MusicEditor`](MusicEditor.kt) | Edits the music in focus in `MusicStudio`, its track list and what it focuses |
| [`TrackEditor`](TrackEditor.kt) | Edits the track in focus: its notations, arrangements and the sections each plays |
| [`NotationEditor`](NotationEditor.kt) | Edits one notation of the track in focus through its text |
| [`MusicForm`](MusicForm.kt) | Holds a music's key, time, tempo and capo apart from the music until they are applied |
| [`LaneEditor`](LaneEditor.kt) | Lays a song's tracks out as lanes of notations on a timeline, and plays them from its cursor |
| [`NoteDrag`](NoteDrag.kt) | A note dragged in a lane, and the snapping and edit it makes |
| [`NotationTextEditor`](NotationField.kt) | The text of a notation and the problems found reading it, which the notation editors share |
| [`StudioIdbDb`](StudioIdbDb.kt) | The `StudioDb` kept in the browser, and its DAOs |
| [`ClipEditor`](ClipEditor.kt) | Binds the clip of a track or notation, chosen among those that fit it |
| [`ClipMic`](ClipMic.kt) | Records a clip of a track or notation after a counted lead-in, keeping it only when it reaches the end |
| [`WaveShelf`](MusicWave.kt) | Reads the clips bound to a music that still fit it, decoded for playback |
| [`TimelineModel`](TimelineModel.kt) | The state a timeline shows in `StudioEditor` |
| [`StudioEditor`](StudioEditor.kt) | Records and plays samples on a timeline |
| [`SiteMonitor`](SiteMonitor.kt) | Charts the site's status |
| [`Sandbox`](Sandbox.kt) | The state of the sandbox |
| [`LocationOnboarder`](LocationOnboarder.kt) | Finds a location owner's location in Streetlight or OpenStreetMap |

### Map

| Model | Role |
|---|---|
| [`Earth`](Earth.kt) | The model of the map screens |
| [`EarthCache`](EarthCache.kt) | The events of each settled map view |
| [`setPoints`, `showStarLocation`…](markerMapPoints.kt) | Sets a `MarkerMap`'s markers from entities and shows the viewer's location on it |
| [`StreetMarker`](StreetMarker.kt) | The markers of entities |
| [`InflateMarker`](InflateMarker.kt) | The markers of event groups |
| [`StarMarker`](StarMarker.kt) | The viewer's own location marker |
| [`PrototypeMarker`](PrototypeMarker.kt) | The marker id of a spirit |
| [`MapTypeIcon`](MapTypeIcon.kt) | The icon of a location marker by its map type |
| [`StarLocator`](StarLocator.kt) | The viewer's location |
| [`SpiritMap`](SpiritMap.kt) | The map's position shared with other viewers |
| [`TransitMap`](TransitMap.kt) | Transit routes and vehicle positions |
