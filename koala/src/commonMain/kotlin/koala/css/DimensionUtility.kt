package koala.css

import kotlinx.css.GridTemplateColumns
import kotlinx.css.LinearDimension

fun columnsOf(vararg dims: LinearDimension) = GridTemplateColumns(*dims)