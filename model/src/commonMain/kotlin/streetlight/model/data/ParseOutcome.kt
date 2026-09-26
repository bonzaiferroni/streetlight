package streetlight.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The outcome of parsing a page with its schema. */
@Serializable
enum class ParseOutcome {
    /** Every event the page held was built. */
    @SerialName("complete") Complete,
    /** Some event data could not be built, such as date text that did not parse. */
    @SerialName("partial") Partial,
    /** The parser could not read the page, such as with a schema that failed validation. */
    @SerialName("fail") Fail,
}
