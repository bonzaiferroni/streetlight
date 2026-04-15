package koala.html

import koala.css.Prose

// language="CSS"
val TableCss get() = """
$Prose th,
$Prose td {
    padding: .5rem 0.75rem;
}

$Prose table {
    border-collapse: separate;
    border-spacing: 0;
}

$Prose th + th,
$Prose td + td {
    border-left: 1px solid rgba(var(--ink), .2);;
}

$Prose th {
    text-align: inherit;
}

"""