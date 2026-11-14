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
| [`markdown`](markdown.kt) | Markdown rendered, from text or parsed blocks |

### Editors

| Part | Role |
|---|---|
| [`markdownEditor`](styledMarkdownEditor.kt) | An editor that styles its markdown as it is typed |
| [`textEditor`](textEditor.kt) | An editable plain-text area as tall as its lines, bound to text or markdown |
| [`MarkdownEditor`](MarkdownEditor.kt) | The parsed blocks of an editor, cached by chunk |
| [`MarkdownEditorRenderer`](MarkdownEditorRenderer.kt) | Renders a parsed block as editor markup |
| [`MarkdownInputParser`](MarkdownInputParser.kt) | Reads an editor's DOM after input and matches it to the parsed markdown |
| [`createEditorBlock`, `syncEditorBlock`](renderEditorBlock.kt) | An editor block element, and its update to a new block |
| [`renderEditorTable`](renderEditorTable.kt) | Renders a table block in an editor |
| [`EditorSegment`](EditorSegment.kt) | A range of a chunk and the class it is styled with |
| [`handleTableNavigation`](handleTableNavigation.kt) | Moves the caret between table cells on an arrow key |
| [`CaretUtility`](CaretUtility.kt) | Reads and places the caret within an element's text |
| [`MarkdownEditorUtility`](MarkdownEditorUtility.kt) | An element's text, normalized for the parser |
