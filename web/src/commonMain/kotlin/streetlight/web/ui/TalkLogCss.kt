package streetlight.web.ui

import koala.css.Class

object CommentClass {
    val Comment = Class("comment")
    val NestedContent = Class("comment__nested-content")
    val HasNestedContent = Class("comment--has-nested")
    val ChildColumn = Class("comment__child-column")
    val IsEditing = Class("comment--is-editing")
    val IsReplying = Class("comment--is-replying")
    val Body = Class("comment__body")
    val Editor = Class("comment__editor")
    val Content = Class("comment__content")
    val Reply = Class("comment__reply")
}

// language="CSS"
val TalkLogCss get() = """
${CommentClass.Comment} {
    
}

${CommentClass.NestedContent} {
    position: relative;
}

${CommentClass.HasNestedContent} > div > ${CommentClass.NestedContent}::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: var(--unit-spacing-2);
    height: var(--unit-spacing-2);
    background: radial-gradient(circle at 100% 100%, transparent var(--unit-spacing-2), var(--zen-card-bg) var(--unit-spacing-2));
}

${CommentClass.HasNestedContent} > div > ${CommentClass.NestedContent} {
    padding: var(--unit-spacing) 0 0 var(--unit-spacing);
}

${CommentClass.HasNestedContent} > .card {
    border-radius: var(--unit-spacing-2) var(--unit-spacing-2) var(--unit-spacing-2) 0;
}

${CommentClass.Content},
${CommentClass.Editor} {
    transition: var(--transition-opacity);
}

${CommentClass.Reply} {
    display: none;
}

${CommentClass.Reply},
${CommentClass.Editor} {
    opacity: 0;
    pointer-events: none;
    height: 0;
}

${CommentClass.Reply}${CommentClass.IsReplying},
${CommentClass.IsEditing} ${CommentClass.Editor} {
    opacity: 1;
    pointer-events: auto;
    height: auto;
    display: block;
}

${CommentClass.IsEditing} ${CommentClass.Content} {
    opacity: 0;
    pointer-events: none;
}
"""