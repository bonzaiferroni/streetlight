package koala.model

import koala.css.Class
import koala.html.enumAttributeOf
import koala.html.intAttributeOf
import koala.markdown.ContentType

object EditorStyle {
    val Container = Class("mde")

    val BlockType = enumAttributeOf<ContentType>("block-type")

    val Paragraph = Container.withBemElement("paragraph")
    val Heading = Container.withBemElement("heading-line")
    val HorizontalRule = Container.withBemElement("horizontal-rule")
    val Code = Container.withBemElement("code-block")
    val BlockQuote = Container.withBemElement("blockquote")
    val UnorderedList = Container.withBemElement("unordered-list")
    val OrderedList = Container.withBemElement("ordered-list")
    val Table = Container.withBemElement("table")
    val BlockImage = Container.withBemElement("block-image")

    val Extra = Container.withBemElement("extra")
    val Emphasis = Container.withBemElement("emphasis")
    val Strong = Container.withBemElement("strong")
    val InlineCode = Container.withBemElement("inline-code")
    val Text = Container.withBemElement("text")
    val LinkText = Container.withBemElement("link-text")
    val Url = Container.withBemElement("url")

    val HeadingLevel = intAttributeOf("heading-level")
    val HeadingFiligree = Container.withBemModifier("filigree")
}

// language="CSS"
val TextEditorCss get() = with(EditorStyle) { """
$Container {
    font-family: ui-monospace, Menlo, Consolas, monospace;
    outline: none;
    padding: var(--unit-spacing);
    font-size: var(--paragraph-size);
    background-color: var(--void-bg);
    color: rgb(var(--ink));
    line-height: var(--paragraph-line-height);
    white-space: pre-wrap;

    border: 1px solid var(--void-border);
    border-radius: var(--unit-spacing);
    box-shadow: var(--input-shadow);
    
    > * {
        min-height: 1lh;
    }
}

$Heading {
    font-weight: bold;
    font-size: 2rem;
    text-align: center;
    
    &${HeadingLevel.selector(1)} {
        font-family: var(--font-family);
        font-size: var(--heading-1-size);
        font-weight: var(--heading-1-weight);
    }
    
    &${HeadingLevel.selector(2)} {
        font-family: var(--font-family);
        font-size: var(--heading-2-size);
        font-weight: var(--heading-2-weight);
    }
    
    &${HeadingLevel.selector(3)} {
        font-family: var(--font-family);
        font-size: var(--heading-3-size);
        font-weight: var(--heading-3-weight);
    }
    
    &${HeadingLevel.selector(4)} {
        font-family: var(--font-family);
        font-size: var(--heading-4-size);
        font-weight: var(--heading-4-weight);
    }
    
    &${HeadingLevel.selector(5)} {
        font-family: var(--font-family);
        font-size: var(--heading-5-size);
        font-weight: var(--heading-5-weight);
    }
    
    &${HeadingLevel.selector(6)} {
        font-family: var(--font-family);
        font-size: var(--paragraph-size);
    }
    
    &$HeadingFiligree {
        display: flex;
        align-items: center;
        gap: 1rem;
        
        &::before,
        &::after {
            content: "";
            flex: 1;
            height: 2px;
            background-color: currentColor;
            opacity: var(--opacity-low);
        }
    }
}

$Emphasis {
    font-style: italic;
}

$Strong {
    font-weight: bold;
}

$InlineCode {
    color: var(--green-fg);
    background: var(--zen-bg);
    border-radius: 2px;
}

$Extra {
    color: var(--primary-fg);
}
""" }