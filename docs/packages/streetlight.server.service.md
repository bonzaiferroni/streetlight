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
