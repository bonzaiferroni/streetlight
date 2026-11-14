# Package streetlight.compose.io

## Introduction

The app's transport to the Streetlight server, its cookies and files, and the loader of its images.

## Dependencies

| Package | Provides |
|---|---|
| `io.ktor.client` | HTTP, event streams and sockets, over CIO |
| `kampfire.api` | `ApiTransport` |
| `coil3` | The image loader |

## Web Counterparts

| Compose | Web |
|---|---|
| `AppTransport` | `WebTransport` (`streetlight.web.io`) |
| `AppCookieStorage` | The browser's cookies |
| `OsmClient` | `OSMClient` (`streetlight.web.io`), naming the app to OpenStreetMap as its policy asks |

## Transport

`AppTransport` is the app's `ApiTransport`, over Ktor. `appModule` registers it with the server's URL from `AppEnv` and builds `ApiClient` over it.

`AppCookieStorage` keeps the cookies in an app-data file, so a session outlives the app. A cookie's `Max-Age` is saved as an absolute expiry. When the base URL is plain `http`, the storage clears `secure` on the cookies it keeps, since Ktor sends a secure cookie only over `https` and makes no exception for `localhost` as a browser does.

`AppFiles.kt` reads and writes app-data files; its `actual` in `jvmMain` places them where each desktop OS keeps app data and writes them readable by the user alone. `readAppFileNow` reads at once, for a value wanted before the app goes on.

A model's state that outlasts the app is a store made by `localStoreOf`, the cousin of the web's `localStoreOf`: it starts from its file, read at once so the app opens on it rather than on its defaults, and saves each change as json once the change settles. Its state is `@Serializable`, and a field added or removed later still reads.

## Images

An image is shown with Coil's `AsyncImage`, given the image's `Url`. The loader `buildImageLoader` makes, which `App()` sets, resolves a relative `Url` against `AppEnv.apiUrl`.
