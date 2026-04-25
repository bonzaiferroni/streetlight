package streetlight.web.ui

import koala.css.Class

object TalkLogClass {
    val Comment = Class("talk-comment")
    val NestedContent = Class("talk-comment__nested-content")
    val HasNestedContent = Class("talk-comment--has-nested")
    val ChildColumn = Class("talk-comment--child-column")
}

// language="CSS"
val TalkLogCSS get() = """
${TalkLogClass.Comment} {
    
}

${TalkLogClass.NestedContent} {
    position: relative;
}

${TalkLogClass.HasNestedContent} > div > ${TalkLogClass.NestedContent}::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: var(--unit-spacing-2);
    height: var(--unit-spacing-2);
    background: radial-gradient(circle at 100% 100%, transparent var(--unit-spacing-2), var(--zen-card-bg) var(--unit-spacing-2));
}

${TalkLogClass.HasNestedContent} > div > ${TalkLogClass.NestedContent} {
    padding: var(--unit-spacing) 0 0 var(--unit-spacing);
}

${TalkLogClass.HasNestedContent} > .card {
    border-radius: var(--unit-spacing-2) var(--unit-spacing-2) var(--unit-spacing-2) 0;
}
"""