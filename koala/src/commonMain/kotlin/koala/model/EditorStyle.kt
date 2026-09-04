package koala.model

import koala.css.Class
import koala.html.enumAttributeOf
import koala.html.intAttributeOf
import koala.markdown.ContentBlock

object EditorStyle {
    val Container = Class("mde")
    val SWYG = Class("swyg")

    val BlockType = enumAttributeOf<ContentBlock>("block-type")

    val Extra = Container.withBemElement("extra")
    val Emphasis = Container.withBemElement("emphasis")
    val Strong = Container.withBemElement("strong")
    val InlineCode = Container.withBemElement("inline-code")
    val Strikethrough = Container.withBemElement("strikethrough")
    val Text = Container.withBemElement("text")
    val LinkText = Container.withBemElement("link-text")
    val Url = Container.withBemElement("url")
    val Space = Container.withBemElement("space")

    val HeadingLevel = intAttributeOf("heading-level", true)
    val HeadingFiligree = Container.withBemModifier("filigree")

    val TableRow = Container.withBemElement("table-row")
    val TableCell = Container.withBemElement("table-cell")
    val TableTail = Container.withBemElement("table-tail")

    val NoEdit = Container.withBemModifier("no-edit")
    val WithImage = Container.withBemModifier("with-image")
}

// language="CSS"
val TextEditorCss get() = with(EditorStyle) { """
$Container {
    display: flow-root;
    outline: none;
    padding: var(--unit-spacing);
    font-size: var(--paragraph-size);
    background-color: var(--zen-bg);
    color: rgb(var(--ink));
    line-height: var(--paragraph-line-height);
    white-space: pre-wrap;

    border: 2px solid var(--void-border);
    border-radius: var(--unit-spacing);
    box-shadow: var(--input-shadow);
    
    > * {
        min-height: 1lh;
    }
    
    ${BlockType.selector(ContentBlock.Heading)} {
        font-weight: bold;
        font-size: 2rem;
        text-align: center;
        font-family: var(--font-family);
        
        &${HeadingLevel.selector(1)} {
            font-size: var(--heading-1-size);
            font-weight: var(--heading-1-weight);
        }
        
        &${HeadingLevel.selector(2)} {
            font-size: var(--heading-2-size);
            font-weight: var(--heading-2-weight);
        }
        
        &${HeadingLevel.selector(3)} {
            font-size: var(--heading-3-size);
            font-weight: var(--heading-3-weight);
        }
        
        &${HeadingLevel.selector(4)} {
            font-size: var(--heading-4-size);
            font-weight: var(--heading-4-weight);
            text-transform: uppercase;
        }
        
        &${HeadingLevel.selector(5)} {
            font-size: var(--heading-5-size);
            font-weight: var(--heading-5-weight);
        }
        
        &${HeadingLevel.selector(6)} {
            font-size: var(--paragraph-size);
        }
        
        &$HeadingFiligree {
            overflow: clip;
            text-overflow: clip;
            &::before,
            &::after {
                content: "";
                display: inline-block;
                width: 50%;
                height: 2px;
                vertical-align: middle;
                background-color: currentColor;
                opacity: var(--opacity-low);
            }
            
            &::before { margin-inline: -50% 1rem; }
            &::after { margin-inline: 1rem -50%; }
        }
    }
    
    ${BlockType.selector(ContentBlock.Code)} {
        font-family: var(--mono-family);
        background: var(--zen-bg);
        border-radius: 2px;
    }
    
    ${BlockType.selector(ContentBlock.Image)},
    ${BlockType.selector(ContentBlock.Paragraph)} {
        &$WithImage {
            &::before {
                content: "";
                background-image: var(--inline-image);
                background-size: cover;
                border-radius: var(--unit-spacing-1);
                float: right;
                clear: right;
                width: 20%;
                aspect-ratio: 3 / 2;
                margin-left: 1rem;
                margin-bottom: 1rem;
            }
        }
    }
    
    ${BlockType.selector(ContentBlock.Table)} {
        font-family: var(--mono-family);
    }
    
    /* Inline styling */
    
    $Emphasis {
        font-style: italic;
    }
    
    $Strong {
        font-weight: bold;
    }
    
    $InlineCode {
        font-family: var(--mono-family);
        color: var(--green-fg);
        border-radius: 2px;
    }
    
    $Strikethrough {
        text-decoration: line-through;
    }
    
    :not(${BlockType.selector(ContentBlock.Code)}) {
        $InlineCode {
            background: var(--zen-bg);
        }
    }
    
    $Extra {
        color: var(--primary-fg);
    }
    
    &$SWYG {
        $Extra {
            display: none;
        }
    
        ${BlockType.selector(ContentBlock.Table)} {
           display: grid;
            
            $TableRow {
                display: grid;
                grid-column: 1 / -1;
                grid-template-columns: subgrid;
            }
            
            $TableTail {
                white-space: nowrap;
            }
        }
    }
    
    $NoEdit {
        user-select: none;
    }
}
""" }