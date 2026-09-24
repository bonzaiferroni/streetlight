package kampfire.utils

/** Query parameters, each key mapped to its values. */
typealias ParameterMap = Map<String, List<String>>

/** The first value under [id] as a float, or `null`. */
fun ParameterMap.readFloat(id: String) = this[id]?.firstOrNull()?.toFloatOrNull()
/** The values under [id] that read as floats. */
fun ParameterMap.readFloatList(id: String) = this[id]?.mapNotNull { it.toFloatOrNull() }
/** The values under [id] that read as doubles. */
fun ParameterMap.readDoubleList(id: String) = this[id]?.mapNotNull { it.toDoubleOrNull() }