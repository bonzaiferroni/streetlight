package streetlight.server

import klutch.environment.readEnvFromPath

object ServerProvider {
    val env = readEnvFromPath()
}