package kampfire.utils

typealias ParameterMap = Map<String, List<String>>

fun ParameterMap.readFloat(id: String) = this[id]?.firstOrNull()?.toFloatOrNull()
fun ParameterMap.readFloatList(id: String) = this[id]?.mapNotNull { it.toFloatOrNull() }
fun ParameterMap.readDoubleList(id: String) = this[id]?.mapNotNull { it.toDoubleOrNull() }