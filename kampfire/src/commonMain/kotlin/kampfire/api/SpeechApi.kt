package kampfire.api

import kampfire.model.SpeechRequest

/** The endpoints of a speech service, returning audio as WAV data or as a URL. */
interface SpeechApi {
    val wav: PostEndpoint<SpeechRequest, ByteArray>
    val url: PostEndpoint<SpeechRequest, String>
}