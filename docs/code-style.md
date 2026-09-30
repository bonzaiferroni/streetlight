# Code Style

How code is written here, beyond what the package documents specify for their own packages.

## Naming

A type's name follows an AdjectiveNoun shape, such as `FooBar`.

A variable is named from its type, in whole words:

| Case | Name | Example |
|---|---|---|
| The noun alone shadows nothing | The noun | A `FooBar` is `bar` |
| The noun alone shadows another name in scope | The type's name | An `LdEvent` beside an event's `PropertyMap` is `ldEvent` |
| Several values share a type | The noun with the role each plays | `currentBar`, `lastBar`, `cachedBar` |

A name never drops the type for a word that names only where the value came from, such as `declared` for an `LdEvent`, or for a role alone, such as `validated` for a schema.

A `PropertyMap` is named for the record its properties describe, never with `map`: `event`, `location`, and with a role, `pageEvent`, `declaredEvent`, `declaredLocation`.
