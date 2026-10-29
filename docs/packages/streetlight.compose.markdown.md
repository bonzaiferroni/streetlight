# Package streetlight.compose.markdown

## Introduction

Markdown rendered for Compose, from the blocks koala's parser reads.

## Dependencies

| Package | Provides |
|---|---|
| `koala.markdown` | `markdownBlocksOf`, `MarkdownParser`, and the parsed blocks and spans |

## Web Counterparts

| Compose | Web |
|---|---|
| `annotatedMarkdownOf` | `renderMarkdownBlocks` (`koala.markdown`), as a feed row's summary styles it |
| `MarkdownStyler`, `MarkdownStyle` | `MarkdownEditor` and `EditorSegment` (`koala.dom.markdown`), and the classes of `EditorStyle` |

## Rendering

`annotatedMarkdownOf` renders markdown as one `AnnotatedString` for text read as prose: each block on its own line, headings at the text's size a step heavier, emphasis, strong, code and struck text styled, and links in the color given. Images and rules are left out. Links are not yet followed.

## Editing

`MarkdownStyler` gives the styled ranges of markdown text for an editor, the text itself unchanged. It splits the text into chunks at blank lines, a fenced code block held whole, parses only the chunks it has not seen, and keeps each by its text, so a keystroke parses the chunk it lands in. Its ranges are colorless; the editor in `streetlight.compose.view` paints them in the theme.
