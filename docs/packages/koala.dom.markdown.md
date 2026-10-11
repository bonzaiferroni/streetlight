# Package koala.dom.markdown

## Introduction

Markdown rendered in the browser, and the editors that style markdown as it is typed.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom` | `ViewScope` and the view runtime |
| `koala.markdown` | The parsers, the parsed blocks and the editor stylesheet |
| `kampfire.model` | `MutableTap` |
| `web` (Kotlin wrappers) | DOM types and the selection |

## Markdown

### Rendering

| Part | Role |
|---|---|
| [`markdown`](../../koala/src/jsMain/kotlin/koala/dom/markdown/markdown.kt) | Markdown rendered, from text or parsed blocks |

### Editors

| Part | Role |
|---|---|
| [`markdownEditor`](../../koala/src/jsMain/kotlin/koala/dom/markdown/styledMarkdownEditor.kt) | An editor that styles its markdown as it is typed |
| [`basicMarkdownEditor`](../../koala/src/jsMain/kotlin/koala/dom/markdown/basicMarkdownEditor.kt) | An editable plain-text area |
| [`MarkdownEditor`](../../koala/src/jsMain/kotlin/koala/dom/markdown/MarkdownEditor.kt) | The parsed blocks of an editor, cached by chunk |
| [`MarkdownEditorRenderer`](../../koala/src/jsMain/kotlin/koala/dom/markdown/MarkdownEditorRenderer.kt) | Renders a parsed block as editor markup |
| [`MarkdownInputParser`](../../koala/src/jsMain/kotlin/koala/dom/markdown/MarkdownInputParser.kt) | Reads an editor's DOM after input and matches it to the parsed markdown |
| [`createEditorBlock`, `syncEditorBlock`](../../koala/src/jsMain/kotlin/koala/dom/markdown/renderEditorBlock.kt) | An editor block element, and its update to a new block |
| [`renderEditorTable`](../../koala/src/jsMain/kotlin/koala/dom/markdown/renderEditorTable.kt) | Renders a table block in an editor |
| [`EditorSegment`](../../koala/src/jsMain/kotlin/koala/dom/markdown/EditorSegment.kt) | A range of a chunk and the class it is styled with |
| [`handleTableNavigation`](../../koala/src/jsMain/kotlin/koala/dom/markdown/handleTableNavigation.kt) | Moves the caret between table cells on an arrow key |
| [`CaretUtility`](../../koala/src/jsMain/kotlin/koala/dom/markdown/CaretUtility.kt) | Reads and places the caret within an element's text |
| [`MarkdownEditorUtility`](../../koala/src/jsMain/kotlin/koala/dom/markdown/MarkdownEditorUtility.kt) | An element's text, normalized for the parser |
