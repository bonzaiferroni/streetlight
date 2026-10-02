# Package streetlight.server.daemon.unit

## Introduction

Unit tests for the daemon, in the test source set. Each covers a rule of one function or type that holds with no collaborators.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.server.daemon.agent` | Parsing, structured data and fetch text under test |
| `streetlight.server.daemon.crawler` | Parse utilities, location rules and the robots gate under test |
| `kotlin.test` | Assertions |

`docs/testing.md` states how tests are written. The package is flat: one file per unit under test, whatever package it lives in.
