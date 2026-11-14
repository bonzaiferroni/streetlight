# Package koala.dom.page

## Introduction

The browser side of the page frame: reading data islands, adopting server-rendered shells and loading scripts.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom` | `ViewScope` and the view runtime |
| `koala.page` | `Id` and the data islands written by the server |
| `koala.model` | `ScriptLoader` |

## Page Frame

| Part | Role |
|---|---|
| [`readIsland`](dataIsland.kt) | Reads a data island written by `dataIsland` |
| [`shellBox`, `shellBoxWithMap`](shellBox.kt) | Adopts the server-rendered shell, or builds it when there is none |
| [`lazyScript`](lazyScript.kt) | Loads a script once, then builds content |
