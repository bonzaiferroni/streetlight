package streetlight.web.ui

import kabinet.utils.olderThan
import koala.dom.ViewScope
import streetlight.model.data.Message
import streetlight.model.ui.Screen
import streetlight.web.io.OmniClient
import kotlin.time.Duration.Companion.days

fun ViewScope.wireOmni(omni: OmniClient) {
    val screenState = portal.screenState

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
                val record = record ?: return@collect
                if (screenState.now == Screen.Inbox && record is Message || record.recordAt.olderThan(1.days)) return@collect
                toaster.deliver(record.text)
            }
        }
    }
}