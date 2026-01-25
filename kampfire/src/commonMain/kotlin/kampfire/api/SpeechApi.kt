package kampfire.api

import kampfire.model.SpeechRequest

interface SpeechApi {
    val wav: PostEndpoint<SpeechRequest, ByteArray>
    val url: PostEndpoint<SpeechRequest, String>
}