package streetlight.web

import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

// Provided by yer JS libs
external class ProtobufRoot {
    fun load(path: String): Promise<ProtobufRoot>
    fun lookupType(path: String): ProtobufType
}

external class ProtobufType {
    fun <T> decode(buffer: Uint8Array): ProtobufMessage<T>
}

external class ProtobufMessage<T> {
    val entity: Array<T>
}

external val protobuf: ProtobufRoot