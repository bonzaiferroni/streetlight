package koala.core

import js.array.asList
import koala.external.LottieOptions
import koala.external.RendererSettings
import koala.external.lottie
import koala.html.Attribute
import web.html.HTMLElement

fun queryAndInitLotties(ancestor: HTMLElement) {
    val elements = ancestor.querySelectorAll(Attribute.Lottie.selector).asList()
    elements.forEach {
        initLottie(it as HTMLElement)
    }
}

fun initLottie(element: HTMLElement) {
    val path = element.getAttribute(Attribute.Lottie.identifier) ?: return
    lottie.loadAnimation(
        LottieOptions(
            container = element,
            renderer = "svg",
            loop = true,
            autoplay = true,
            path = path,
            rendererSettings = RendererSettings(
                preserveAspectRatio = "xMidYMid meet",
            )
        )
    )
}