package streetlight.app.services

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import kotlin.random.Random

class RtmpClient(
    private val host: String,
    private val port: Int = 1935,
    private val app: String,                 // e.g. "app"
    private val streamKey: String,           // path piece after app
    private val tcUrl: String                // e.g. "rtmp://$host/$app"
) {
    private val selector = ActorSelectorManager(Dispatchers.IO)

    private lateinit var input: ByteReadChannel
    private lateinit var output: ByteWriteChannel

    suspend fun connectAndPublish() {
        val socket = aSocket(selector).tcp().connect(host, port)
        input = socket.openReadChannel()
        output = socket.openWriteChannel(autoFlush = true)

        rtmpHandshake()

        // Default chunk size 128; basic chunk stream ID 3 for control/commands.
        sendConnect(app, tcUrl)
        readUntilResult("_result")           // crude; production needs proper parsing

        val streamId = createStream()
        publish(streamId, streamKey)
        // Now start sending FLV-tag payloads via RTMP messages (type 8/9).
    }

    private suspend fun rtmpHandshake() {
        // C0 (version)
        output.writeByte(0x03)
        // C1 (time + zero + random 1528 bytes)
        val c1 = ByteArray(1536)
        writeU32(c1, 0, 0)                   // time
        writeU32(c1, 4, 0)                   // zero
        Random.nextBytes(c1, 8, 1536 - 8)
        output.writeFully(c1)

        // Read S0+S1+S2
        val s0 = input.readByte()
        require(s0.toInt() == 0x03) { "Bad RTMP version: $s0" }
        val s1 = ByteArray(1536)
        input.readFully(s1, 0, 1536)
        val s2 = ByteArray(1536)
        input.readFully(s2, 0, 1536)

        // C2 is S1+echo-ish; for simple client just mirror S1 timing block
        output.writeFully(s1)
    }

    private suspend fun sendConnect(app: String, tcUrl: String) {
        val body = buildAmf0 {
            string("connect")
            number(1.0)                      // txn id
            obj {
                string("app", app)
                string("type", "nonprivate")
                string("tcUrl", tcUrl)
                string("flashVer", "FMLE/3.0 (compatible; FMSc/1.0)")
                bool("fpad", false)
                number("capabilities", 15.0)
                number("audioCodecs", 3575.0)
                number("videoCodecs", 252.0)
                number("videoFunction", 1.0)
                number("objectEncoding", 0.0)
            }
        }
        sendRtmpMessage(
            chunkStreamId = 3,
            messageTypeId = 20,              // AMF0 Command Message
            messageStreamId = 0,
            timestamp = 0,
            payload = body
        )
    }

    private suspend fun createStream(): Int {
        val body = buildAmf0 {
            string("createStream")
            number(2.0)
            nullValue()
        }
        sendRtmpMessage(3, 20, 0, 0, body)
        // TODO: parse _result; for a stub, return 1
        return 1
    }

    private suspend fun publish(streamId: Int, streamKey: String) {
        val body = buildAmf0 {
            string("publish")
            number(3.0)
            nullValue()
            string(streamKey)
            string("live")
        }
        sendRtmpMessage(3, 20, streamId, 0, body)
    }

    private suspend fun sendRtmpMessage(
        chunkStreamId: Int,
        messageTypeId: Int,
        messageStreamId: Int,
        timestamp: Int,
        payload: ByteArray
    ) {
        // Basic Header (fmt=0, csid up to 63)
        output.writeByte((0 shl 6 or (chunkStreamId and 0x3F)).toByte())

        // Message Header (fmt0: 11 bytes)
        write24(output, timestamp and 0xFFFFFF)
        write24(output, payload.size)
        output.writeByte(messageTypeId.toByte())
        write32LE(output, messageStreamId)

        // Extended timestamp if needed (omit for brevity)

        // Chunk the payload (default chunk size 128)
        var off = 0
        val chunkSize = 128
        while (off < payload.size) {
            val n = minOf(chunkSize, payload.size - off)
            output.writeFully(payload, off, n)
            off += n
            if (off < payload.size) {
                // Chunk header with fmt=3 (no header), same csid
                output.writeByte((3 shl 6 or (chunkStreamId and 0x3F)).toByte())
            }
        }
    }

    private suspend fun readUntilResult(expect: String) {
        // Stub: real code must parse RTMP chunks and AMF0
        // Keep draining a bit so servers don’t choke.
        withTimeoutOrNull(500) { input.readAvailable(ByteArray(4096), 0, 4096) }
    }

    // ===== Helpers =====
    private fun writeU32(a: ByteArray, off: Int, v: Int) {
        a[off] = ((v ushr 24) and 0xFF).toByte()
        a[off + 1] = ((v ushr 16) and 0xFF).toByte()
        a[off + 2] = ((v ushr 8) and 0xFF).toByte()
        a[off + 3] = (v and 0xFF).toByte()
    }

    private suspend fun write24(out: ByteWriteChannel, v: Int) {
        out.writeByte(((v ushr 16) and 0xFF).toByte())
        out.writeByte(((v ushr 8) and 0xFF).toByte())
        out.writeByte((v and 0xFF).toByte())
    }

    private suspend fun write32LE(out: ByteWriteChannel, v: Int) {
        out.writeByte((v and 0xFF).toByte())
        out.writeByte(((v ushr 8) and 0xFF).toByte())
        out.writeByte(((v ushr 16) and 0xFF).toByte())
        out.writeByte(((v ushr 24) and 0xFF).toByte())
    }
}

// ------- Tiny AMF0 builder (strings/numbers/null/objects) --------
private class Amf0Builder {
    private val buf = ByteBuffer.allocate(64 * 1024)

    fun string(s: String) {
        buf.put(0x02) // String marker
        val b = s.toByteArray(Charsets.UTF_8)
        require(b.size <= 0xFFFF) { "AMF0 short string too long" }
        buf.putShort(b.size.toShort())
        buf.put(b)
    }

    fun string(key: String, value: String) {
        objPropName(key); string(value)
    }

    fun number(n: Double) {
        buf.put(0x00) // Number
        buf.putLong(java.lang.Double.doubleToRawLongBits(n))
    }

    fun number(key: String, n: Double) {
        objPropName(key); number(n)
    }

    fun bool(key: String, v: Boolean) {
        objPropName(key); buf.put(0x01); buf.put(if (v) 1 else 0)
    }

    fun nullValue() {
        buf.put(0x05)
    }

    fun obj(block: Amf0Builder.() -> Unit) {
        buf.put(0x03) // Object
        val start = buf.position()
        this.block()
        // End object
        buf.put(0x00); buf.put(0x00); buf.put(0x09)
    }

    private fun objPropName(name: String) {
        val b = name.toByteArray(Charsets.UTF_8)
        buf.putShort(b.size.toShort())
        buf.put(b)
    }

    fun toByteArray(): ByteArray {
        val out = ByteArray(buf.position())
        buf.rewind()
        buf.get(out)
        return out
    }
}

private fun buildAmf0(block: Amf0Builder.() -> Unit): ByteArray =
    Amf0Builder().apply(block).toByteArray()