package streetlight.app.audio

class DrumLoopLayer(
    private val drumKit: DrumKit,
    private val tempo: Int,
    private val sampleRate: Int = 44_100,
) : AudioLayer {

    override fun hasContent(position: Int) = true

    override fun sampleAt(position: Int, buffer: ShortArray) {
        val sample = drumKit.bass1 ?: return

        // position: global sample index in the song
        val bufferStart = position.toLong()
        val bufferEnd = bufferStart + buffer.size

        // samples per beat (quarter note)
        val beatLength = (60L * sampleRate) / tempo
        if (beatLength <= 0L) return

        // Start one beat before the buffer, to catch overlapping hits
        var beatIndex = if (bufferStart >= beatLength) {
            (bufferStart / beatLength) - 1L
        } else {
            0L
        }

        while (true) {
            val beatStart = beatIndex * beatLength
            if (beatStart >= bufferEnd) break

            val beatEnd = beatStart + sample.size
            // If this beat ends before the buffer starts, skip ahead
            if (beatEnd <= bufferStart) {
                beatIndex++
                continue
            }

            val localPosition: Int
            val samplePosition: Int

            if (beatStart <= bufferStart) {
                // Buffer starts in the middle of the beat sample
                localPosition = 0
                samplePosition = (bufferStart - beatStart).toInt()
            } else {
                // Beat starts inside this buffer
                localPosition = (beatStart - bufferStart).toInt()
                samplePosition = 0
            }

            buffer.addSample(
                sample = sample,
                position = localPosition,
                samplePosition = samplePosition
            )

            beatIndex++
        }
    }
}