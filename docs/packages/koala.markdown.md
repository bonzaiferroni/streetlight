# Package koala.markdown

## Introduction

Markdown parsed into blocks and spans, and rendered as HTML without the bundle.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | Modifiers and classes |
| `kampfire.api` | `Markdown` |

## Markdown

### Parsing

| Part | Role |
|---|---|
| [`MarkdownParser`, `markdownBlocksOf`](../../koala/src/commonMain/kotlin/koala/markdown/MarkdownParser.kt) | Parses markdown into blocks |
| [`MarkdownListParser`](../../koala/src/commonMain/kotlin/koala/markdown/MarkdownListParser.kt) | Parses lists, nesting sublists |
| [`MarkdownTableParser`](../../koala/src/commonMain/kotlin/koala/markdown/MarkdownTableParser.kt) | Parses tables |
| [`MarkdownSpanParser`](../../koala/src/commonMain/kotlin/koala/markdown/MarkdownSpanParser.kt) | Parses the inline spans of a block |
| [`MarkdownParserUtility`](../../koala/src/commonMain/kotlin/koala/markdown/MarkdownParserUtility.kt) | Positions within a line, for the parsers |
| [`MarkdownBlock`](../../koala/src/commonMain/kotlin/koala/markdown/MarkdownBlock.kt) | A parsed block |
| [`MarkdownSpan`](../../koala/src/commonMain/kotlin/koala/markdown/MarkdownSpan.kt) | A parsed run of inline markdown |
| [`ContentBlock`](../../koala/src/commonMain/kotlin/koala/markdown/ContentBlock.kt) | The kinds of block |

### Rendering

| Part | Role |
|---|---|
| [`markdown`](../../koala/src/commonMain/kotlin/koala/markdown/markdown.kt) | Markdown rendered, cut to a limit when given |
| [`renderMarkdownBlocks`](../../koala/src/commonMain/kotlin/koala/markdown/renderMarkdownBlocks.kt) | Renders blocks as HTML |
| [`renderMarkdownSpans`](../../koala/src/commonMain/kotlin/koala/markdown/renderMarkdownSpans.kt) | Renders spans as inline HTML |
| [`MarkdownStyle`](../../koala/src/commonMain/kotlin/koala/markdown/MarkdownStyle.kt) | The stylesheet of rendered markdown |
| [`TableCss`](../../koala/src/commonMain/kotlin/koala/markdown/table.kt) | The stylesheet of prose tables |
| [`EditorStyle`](../../koala/src/commonMain/kotlin/koala/markdown/EditorStyle.kt) | The stylesheet of the editors in `koala.dom.markdown` |
