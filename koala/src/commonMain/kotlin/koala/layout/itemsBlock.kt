package koala.layout

import koala.modifier.*

object ItemsBlockStyle {
    val Class = Class("items-block")
}

// language="CSS"
val ItemsBlockCss get() = with(ItemsBlockStyle) { """

$Class {
    display: flex;
    flex-direction: column;
}

$Class:empty { display: none; }

/* each item pairs with itself, so items that stay move and items that arrive or leave pop */
$Class$Transitioner > * {
    view-transition-name: match-element;
    view-transition-class: ${PrestoKey.Pop};
}
""" }
