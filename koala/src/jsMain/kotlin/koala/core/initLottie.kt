package koala.core

import koala.external.LottieOptions
import koala.external.RendererSettings
import koala.external.lottie
import koala.html.TagAttribute
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

fun findAndInitLotties(ancestor: HTMLElement) {
    val elements = ancestor.querySelectorAll(TagAttribute.lottie.selector).asList()
    elements.forEach {
        initLottie(it as HTMLElement)
    }
}

fun initLottie(element: HTMLElement) {
    val path = element.getAttribute(TagAttribute.lottie.key) ?: return
    lottie.loadAnimation(
        LottieOptions(
            container = element,
            renderer = "svg",
            loop = true,
            autoplay = true,
            path = path,
            rendererSettings = RendererSettings(
                preserveAspectRatio = "xMidYMid slice",
            )
        )
    )
}