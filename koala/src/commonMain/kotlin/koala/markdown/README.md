# Package koala.markdown

## Introduction

Markdown parsed into blocks and spans, and rendered as HTML without the bundle.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL |
| `koala.modifier` | `modify`, and the classes and styles it applies to elements |
| `kampfire.api` | `Markdown` |

## Markdown

### Parsing

| Part | Role |
|---|---|
| [`MarkdownParser`, `markdownBlocksOf`](MarkdownParser.kt) | Parses markdown into blocks |
| [`MarkdownListParser`](MarkdownListParser.kt) | Parses lists, nesting sublists |
| [`MarkdownTableParser`](MarkdownTableParser.kt) | Parses tables |
| [`MarkdownSpanParser`](MarkdownSpanParser.kt) | Parses the inline spans of a block |
| [`MarkdownParserUtility`](MarkdownParserUtility.kt) | Positions within a line, for the parsers |
| [`MarkdownBlock`](MarkdownBlock.kt) | A parsed block |
| [`MarkdownSpan`](MarkdownSpan.kt) | A parsed run of inline markdown |
| [`ContentBlock`](ContentBlock.kt) | The kinds of block |

### Rendering

| Part | Role |
|---|---|
| [`markdown`](markdown.kt) | Markdown rendered, cut to a limit when given |
| [`renderMarkdownBlocks`](renderMarkdownBlocks.kt) | Renders blocks as HTML |
| [`renderMarkdownSpans`](renderMarkdownSpans.kt) | Renders spans as inline HTML |
| [`MarkdownStyle`](MarkdownStyle.kt) | The stylesheet of rendered markdown |
| [`TableCss`](table.kt) | The stylesheet of prose tables |
| [`EditorStyle`](EditorStyle.kt) | The stylesheet of the editors in `koala.dom.markdown` |

### Urls

Markdown comes from users, so a url it carries is checked before it reaches the page, in [`markdownUrls`](markdownUrls.kt).

| Span | Rendered when | Otherwise |
|---|---|---|
| Link | `isSafeMarkdownUrl`: `http`, `https`, `mailto`, or no scheme | Its text, unlinked |
| Lottie image (`.json`) | `isSiteLottiePath`: a file under `lottiePath` | Nothing; the caption stays |

`initLottie` applies the same path check, so a Lottie loads only from the site's own files whatever wrote the attribute. The full Lottie build is loaded because the site's own animations use expressions; the path check is what keeps outside animations off the page.
