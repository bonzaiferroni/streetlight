# Package streetlight.model.studio

## Introduction

The interfaces of the studio's data on the device, shaped after Room. `StudioDb` is the database holder and hands out one DAO per record of `streetlight.model.music`. Each platform implements them over its own storage.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model.music` | The records |
| `kotlinx.coroutines` | `Flow` |

## Naming

| Type | Name |
|---|---|
| The DAO of a record `Foo` | `FooDao` |
| An implementation over a platform's storage | `FooIdbDao`, `FooRoomDao`, named for the storage |

| Function | Name |
|---|---|
| A query that emits again as its rows change | `flowFoo` or `flowFoos`, returning a `Flow` |
| A one-shot read | `readFoo`, `suspend` |
| A write | `saveFoo` or `removeFoo`, `suspend` |

## DAOs

A DAO speaks in records of the domain model, never in the rows of its storage. A query emits again whenever a table it reads changes, and only when its result differs.

Removing a record removes each record that references it by id, as its KDoc states. A save does not check that the records it references exist.

A read or write that spans records, such as `readMusicSheet`, belongs to the DAO of the record it is centered on, and runs as one transaction.

A failed read or write throws, as Room does.
