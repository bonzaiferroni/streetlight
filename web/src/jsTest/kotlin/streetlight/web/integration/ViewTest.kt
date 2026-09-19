package streetlight.web.integration

import koala.dom.AppContainer
import koala.dom.View
import koala.dom.ViewScope
import koala.dom.mountRootView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import streetlight.web.io.ApiClient
import web.dom.document
import web.html.HTMLElement
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class ViewTest {

    private val mounts = mutableListOf<HTMLElement>()

    protected lateinit var scope: CoroutineScope
    protected lateinit var app: AppContainer

    @BeforeTest
    fun prepareView() {
        scope = MainScope()
        app = buildTestApp(scope, api())
    }

    @AfterTest
    fun clearView() {
        scope.cancel()
        mounts.forEach { it.remove() }
        mounts.clear()
    }

    protected open fun api(): ApiClient = error("a test mounting a view supplies its api")

    protected fun mount(block: ViewScope.() -> Unit): View {
        val element = document.createElement("div") as HTMLElement
        document.body?.appendChild(element) ?: error("no body to mount into")
        mounts.add(element)
        return element.mountRootView(MOUNT_NAME, scope, app, block)
    }
}

private const val MOUNT_NAME = "view-test"
