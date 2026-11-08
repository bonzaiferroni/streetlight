package streetlight.web.model

import kotlin.random.Random

/**
 * Wraps mono Opus [packets] in an Ogg file a decoder can read.
 *
 * Each packet holds [OpusPacket.samples] at 48 kHz. The decoder skips the first [preSkip] samples, the encoder's
 * priming, and stops after [sampleCount], dropping the padding of the last packet. [inputSampleRate] notes the rate
 * the audio was captured at.
 */
fun oggOpusOf(packets: List<OpusPacket>, preSkip: Int, sampleCount: Long, inputSampleRate: Int): ByteArray {
    val writer = OggWriter(Random.nextInt())
    writer.writePage(listOf(opusHeadOf(preSkip, inputSampleRate)), granule = 0, headerType = BEGIN_OF_STREAM)
    writer.writePage(listOf(opusTags()), granule = 0, headerType = 0)

    var granule = preSkip.toLong()
    val end = preSkip + sampleCount
    var page = mutableListOf<ByteArray>()
    var segments = 0
    var pageSamples = 0
    packets.forEachIndexed { index, packet ->
        val packetSegments = packet.bytes.size / 255 + 1
        if (segments + packetSegments > MAX_SEGMENTS || pageSamples >= PAGE_SAMPLES) {
            writer.writePage(page, granule, headerType = 0)
            page = mutableListOf()
            segments = 0
            pageSamples = 0
        }
        page.add(packet.bytes)
        segments += packetSegments
        pageSamples += packet.samples
        granule += packet.samples
        if (index == packets.lastIndex) writer.writePage(page, minOf(granule, end), headerType = END_OF_STREAM)
    }
    if (packets.isEmpty()) writer.writePage(emptyList(), preSkip.toLong(), headerType = END_OF_STREAM)
    return writer.toByteArray()
}

/** An Opus packet, holding [samples] at 48 kHz. */
class OpusPacket(val bytes: ByteArray, val samples: Int)

/** The pre-skip of an Opus identification header, or `null` when [head] is not one. */
fun preSkipOf(head: ByteArray): Int? {
    if (head.size < OPUS_HEAD_SIZE || head.decodeToString(0, 8) != "OpusHead") return null
    return (head[10].toInt() and 0xff) or ((head[11].toInt() and 0xff) shl 8)
}

private fun opusHeadOf(preSkip: Int, inputSampleRate: Int) = ByteArray(OPUS_HEAD_SIZE).apply {
    "OpusHead".encodeToByteArray().copyInto(this)
    this[8] = 1 // version
    this[9] = 1 // channels
    putLittleEndian(10, preSkip.toLong(), 2)
    putLittleEndian(12, inputSampleRate.toLong(), 4)
    // output gain and channel mapping family stay 0
}

private fun opusTags(): ByteArray {
    val vendor = VENDOR.encodeToByteArray()
    return ByteArray(8 + 4 + vendor.size + 4).apply {
        "OpusTags".encodeToByteArray().copyInto(this)
        putLittleEndian(8, vendor.size.toLong(), 4)
        vendor.copyInto(this, 12)
        // no user comments
    }
}

/** Writes the pages of one logical Ogg stream with the serial number [serial]. */
private class OggWriter(private val serial: Int) {
    private val pages = mutableListOf<ByteArray>()

    fun writePage(packets: List<ByteArray>, granule: Long, headerType: Int) {
        val lacing = packets.flatMap { packet -> List(packet.size / 255) { 255 } + packet.size % 255 }
        val bodySize = packets.sumOf { it.size }
        val page = ByteArray(PAGE_HEADER_SIZE + lacing.size + bodySize)
        "OggS".encodeToByteArray().copyInto(page)
        page[4] = 0 // version
        page[5] = headerType.toByte()
        page.putLittleEndian(6, granule, 8)
        page.putLittleEndian(14, serial.toLong(), 4)
        page.putLittleEndian(18, pages.size.toLong(), 4)
        page[26] = lacing.size.toByte()
        lacing.forEachIndexed { index, size -> page[PAGE_HEADER_SIZE + index] = size.toByte() }
        var offset = PAGE_HEADER_SIZE + lacing.size
        packets.forEach { packet ->
            packet.copyInto(page, offset)
            offset += packet.size
        }
        page.putLittleEndian(22, crcOf(page).toLong(), 4)
        pages.add(page)
    }

    fun toByteArray(): ByteArray {
        val bytes = ByteArray(pages.sumOf { it.size })
        var offset = 0
        pages.forEach { page ->
            page.copyInto(bytes, offset)
            offset += page.size
        }
        return bytes
    }
}

private fun ByteArray.putLittleEndian(offset: Int, value: Long, size: Int) {
    for (index in 0 until size) this[offset + index] = (value ushr (8 * index)).toByte()
}

/** The Ogg checksum: CRC-32 with the polynomial 0x04C11DB7, unreflected, starting at 0. */
private fun crcOf(bytes: ByteArray): Int {
    var crc = 0
    for (byte in bytes) crc = (crc shl 8) xor CRC_TABLE[((crc ushr 24) xor (byte.toInt() and 0xff)) and 0xff]
    return crc
}

private val CRC_TABLE = IntArray(256) { index ->
    var r = index shl 24
    repeat(8) { r = if (r and 0x80000000.toInt() != 0) (r shl 1) xor 0x04C11DB7 else r shl 1 }
    r
}

private const val BEGIN_OF_STREAM = 0x02
private const val END_OF_STREAM = 0x04
private const val PAGE_HEADER_SIZE = 27
private const val MAX_SEGMENTS = 255
private const val PAGE_SAMPLES = 48_000
private const val OPUS_HEAD_SIZE = 19
private const val VENDOR = "streetlight"
