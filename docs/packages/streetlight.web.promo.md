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

`PromoShot` holds the shot's `view` and an `api` that returns the fakes it reads. A shot that shows stored records builds them in its own file and hands them to the fakes through `api`. A shot of a whole page may instead hold the page's content as JSON in a file of its own, as `fizzBuzzContent.kt` holds the data island of FizzBuzz's page from the dev server, decoded with `jsonConfig`.

`main()` adds the site stylesheet from `buildSiteStyles()` and mounts the shot named by the `shot` query parameter as the body. A new shot is added to `shots` in `Main.kt`.

A shot holds the content only. The camera, the pointer, and the timing belong to its script in `tools/promo`, specified in `tools.promo.md`.

## Building

The compilation is not part of web's webpack bundle. `buildShots()` in `tools/promo/promo.mjs` runs `:web:jsPromoPromoDevelopmentExecutableCompileSync` and bundles its output with esbuild into `tools/promo/bundle`. `serveShot` serves the bundle, `www`, and a page that loads it.
