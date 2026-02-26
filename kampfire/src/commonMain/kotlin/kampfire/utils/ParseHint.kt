package kampfire.utils

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ParseHint(
    val value: String
)