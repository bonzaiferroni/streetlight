package streetlight.web.ui

import koala.dom.ViewScope
import streetlight.web.io.OmniClient

fun ViewScope.wireOmni(omni: OmniClient) {
    launchEffect("wire omni") {
        launch {
            session.starState.flow.collect { star ->
                when (star) {
                    null -> omni.disconnect()
                    else -> omni.connect()
                }
            }
        }

        launch("collect records") {
            omni.lastRecordState.flow.collect { record ->
                toaster.deliver(record?.text ?: return@collect)
            }
        }
    }
}