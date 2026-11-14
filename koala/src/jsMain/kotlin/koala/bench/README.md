# Package koala.bench

## Introduction

Utilities for measuring the performance of code in the browser.

## Dependencies

| Package | Provides |
|---|---|
| `web.performance` | The User Timing API: marks and measures |

## Measures

A measured block is wrapped in `markAndMeasure`, named for the function it measures, such as `"defineClusters"`. Its measure appears in the Timings track of a browser Performance profile.
