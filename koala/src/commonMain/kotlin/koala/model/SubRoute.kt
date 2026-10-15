package koala.model

/** A route among the several of type [R] that one screen shows, beneath its [parent]. */
interface SubRoute<R: SubRoute<R>>: AppRoute {
    /** The route above this one, or `null` for the screen's index. */
    val parent: R?
}

/** The routes from the index down to this one. */
fun <R: SubRoute<R>> R.lineage(): List<R> = generateSequence(this) { it.parent }.toList().asReversed()
