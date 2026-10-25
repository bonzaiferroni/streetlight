# Package streetlight.compose.markdown

## Introduction

Markdown rendered for Compose, from the blocks koala's parser reads.

## Dependencies

| Package | Provides |
|---|---|
| `koala.markdown` | `markdownBlocksOf` and the parsed blocks and spans |

## Web Counterparts

| Compose | Web |
|---|---|
| `annotatedMarkdownOf` | `renderMarkdownBlocks` (`koala.markdown`), as a feed row's summary styles it |

## Rendering

`annotatedMarkdownOf` renders markdown as one `AnnotatedString` for text read as prose: each block on its own line, headings at the text's size a step heavier, emphasis, strong, code and struck text styled, and links in the color given. Images and rules are left out. Links are not yet followed.
