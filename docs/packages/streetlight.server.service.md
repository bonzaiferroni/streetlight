# Package streetlight.server.service

## Introduction

The server's work: the operations a route, a daemon or a webhook calls, which coordinate daos and clients rather than touching a table directly.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.server.model` | `DaoScope`, `DataScope` and the client interfaces |
| `streetlight.server.db.dao` | The daos, reached through `dao` |
| `streetlight.server.db.tables` | Tables an operation names, such as an `ImageColumnTable` |
| `streetlight.model.data` | DTOs |
| `kampfire.model` | `Outcome` |

## Naming

A file holds the operations of one concept and is named for it, as `FooService.kt`. The concept need not be a type: `ImageService.kt` holds functions, and becomes a class only when the work needs state of its own. A service that holds state, such as `ConnectionService`, is a class registered in `serverModule`.

## Scope

An operation is a function on the narrowest scope it uses, following `streetlight.server.model`: `DaoScope` for work over daos alone, `DataScope` once it reaches a client. A function that gains a client call changes its receiver and stays in this package.

## Outside the Process

An operation reaches the outside through `client`, never through a client of its own. `ImageService.kt` downloads through `client.image`, so a test replaces the download with a fake.

## Credential Changes

An account that has a viable email confirms a change to its email or password with its current password. An account without one changes either on its session alone: declining an email never costs a star control of their account's security.

## Image Intake

An image comes in as an upload or as a url the server downloads, and both are bounded before anything decodes them.

| Guard | Where | Rule |
|---|---|---|
| Address | `HttpImageClient` | `http` or `https`, every resolved address public; each redirect hop checked again, at most 3 |
| Time | `HttpImageClient` | Connect, socket and request timeouts |
| Bytes | `HttpImageClient`, `UploadImage` | `MAX_IMAGE_BYTES`, checked against `Content-Length` and enforced while reading |
| Pixels | `encodeImage` | The header's dimensions and frame count against `MAX_FRAME_PIXELS` and `MAX_TOTAL_PIXELS`; an unreadable header is refused |

## Audio Intake

An audio file comes in as an upload alone. `encodeAudioAndStore` reads its duration through `client.audio` before encoding, so a file is refused on `MAX_AUDIO_BYTES` or `MAX_AUDIO_DURATION` before the encoder runs. Every file is stored as Opus in an mp4 container, with no variants, and recorded in `AudioTable`.

An audio post names its audio by id in its meta. `checkMediaAudio` replaces the meta and url of the edit with the stored audio, which must be the caller's, and clears the meta of any other post.
