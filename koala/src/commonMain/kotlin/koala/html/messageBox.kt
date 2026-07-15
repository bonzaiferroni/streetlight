package koala.html

import koala.css.Class

object MessageBox {
    val Class = Class("message-box")
    val Error = Class("message-box--error")
    val Success = Class("message-box--success")
}

// language="CSS"
val MessageBoxCss get() = with(MessageBox) { """
$Class {
    position: relative;
    background-color: var(--primary-card-bg);
    border-radius: calc(var(--unit-spacing));
    overflow: hidden;
    white-space: pre-wrap;
    
    &$Error {
        background-color: var(--error-bg);
    }
    
    &$Success {
        background-color: var(--success-bg);
    }
}

$Class::after {
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
""" }