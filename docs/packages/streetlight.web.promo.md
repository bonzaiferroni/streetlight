# Package streetlight.web.promo

## Introduction

The content of promo videos. A shot mounts real views of the app over the test fakes, so a video shows the data the shot gives it and needs no server or database.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom` | Mounting the shot |
| `streetlight.web.integration` | `buildTestApp` |
| `streetlight.web.io` | `TestApiClient` and the `Test*Client` fakes |
| `streetlight.web.view` and the rest of web | The views a shot shows |

The package is the `promo` compilation of web's js target, in `web/src/jsPromo`. It is associated with `main` and `test`, so it sees both, and nothing in either sees it.

## Naming

| Thing | Name |
|---|---|
| A shot's content | `fooShot`, a `PromoShot` in `fooShot.kt` |
| A shot's capture script | `tools/promo/foo.mjs` |
| The `shot` query parameter | `foo` |

## Shots

`PromoShot` holds the shot's `view` and an `api` that returns the fakes it reads. A shot that shows stored records builds them in its own file and hands them to the fakes through `api`.

`main()` adds the site stylesheet from `buildSiteStyles()` and mounts the shot named by the `shot` query parameter as the body. A new shot is added to `shots` in `Main.kt`.

A shot holds the content only. The camera, the pointer, and the timing belong to its script in `tools/promo`.

## Capture

A script films its shot in `openStudio` from `tools/promo/promo.mjs`: Chrome's headless shell with begin frame control and virtual time. Each `frame()` advances time by one frame, sends that frame's mouse input, and draws. The page's transitions, animations, timers and coroutines run on that time unaltered, so a script never freezes or seeks the page's clock.

The page renders its whole viewport at the quality scale. A script frames the shot with a camera, a rectangle in page pixels that `frameWriter` crops and scales to the output size, so the viewport is sized for the widest frame and the scale for the closest.

## Building

The compilation is not part of web's webpack bundle. `buildShots()` in `tools/promo/promo.mjs` runs `:web:jsPromoPromoDevelopmentExecutableCompileSync` and bundles its output with esbuild into `tools/promo/bundle`. `serveShot` serves the bundle, `www`, and a page that loads it.
