package koala.core

import koala.external.LottieOptions
import koala.external.lottie
import koala.html.Attributes
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

fun findAndInitLotties(ancestor: HTMLElement) {
    val elements = ancestor.querySelectorAll(Attributes.lottie.selector).asList()
    elements.forEach {
        initLottie(it as HTMLElement)
    }
}

fun initLottie(element: HTMLElement) {
    val path = element.getAttribute(Attributes.lottie.key) ?: return
    lottie.loadAnimation(
        LottieOptions(
            container = element,
            renderer = "svg",
            loop = true,
            autoplay = true,
            path = "/www/lottie/$path.json"
        )
    )
}