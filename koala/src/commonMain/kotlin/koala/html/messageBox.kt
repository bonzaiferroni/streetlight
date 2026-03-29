package koala.html

import koala.css.Class

object MessageBoxKey {
    val Class = Class("message-box")
}

// language="CSS"
val MessageBoxCss get() = """
.message-box {
    position: relative;
    background-color: var(--primary-card-bg);
    border-radius: calc(var(--unit-spacing));
    overflow: hidden;
}

.message-box::after {
    content: "";
    position: absolute;
    inset: 0;
    background: white;
    opacity: 0;
    animation: messageGlow 1s ease-in-out 3;
    pointer-events: none;
}

@keyframes messageGlow {
    0%, 100% { opacity: 0; }
    50% { opacity: .05; }
}
"""