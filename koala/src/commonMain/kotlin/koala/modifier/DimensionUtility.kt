package koala.modifier

import kotlinx.css.GridTemplateColumns
import kotlinx.css.LinearDimension

/** Grid columns of [dims]. */
fun columnsOf(vararg dims: LinearDimension) = GridTemplateColumns(*dims)