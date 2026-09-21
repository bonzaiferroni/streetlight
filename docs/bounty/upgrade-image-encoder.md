# Upgrade Image Encoder

`encodeImage.kt` reads images through scrimage. An animated GIF is resized frame by frame, written to an intermediate GIF, and converted by `Gif2WebpWriter`, so every frame passes through a 256-color palette before the WebP step. Scrimage reads WebP through `dwebp`, which cannot decode an animated WebP, so `encodeImage` returns `ImageProblem.AnimatedWebp` for one. `gif2webp` is given one delay, read from the first frame.

Bounty:
* Decide the tool: `img2webp` from libwebp, or ffmpeg with `libwebp_anim`
* Decide how the binary reaches each environment: development, test, and the deploy image
* Encode animated output from resized RGBA frames without the intermediate GIF, keeping `WEBP_QUALITY` and `WEBP_EFFORT`
* Carry each frame's own delay into the output instead of the delay of frame 0
* Decode an animated WebP through the same tool so it takes the animated path, then remove `ImageProblem.AnimatedWebp` and `isAnimatedWebp`
* Run the process on `Dispatchers.IO` with a timeout, and return `ImageProblem.Encoding` when it fails, exits nonzero, or times out
* Write frames to a temporary directory that is deleted on every exit path
* Compare output size and visible quality against the current path on the same animated sources
* Integration tests for `encodeImage` covering an animated GIF, an animated WebP, a tool that is missing, and a timeout
* Docs: the encoder convention in `streetlight.server.routes.md`, once decided
