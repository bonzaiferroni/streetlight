package kampfire.utils

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY
)
/** A hint to a language model on how to read a value into this class or property. */
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ParseHint(
    val value: String
)