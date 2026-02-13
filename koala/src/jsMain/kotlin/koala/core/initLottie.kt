package koala.core

import koala.external.LottieOptions
import koala.external.lottie
import koala.html.Attributes
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

fun findAndInitLottie(ancestor: HTMLElement) {
    val lotties = ancestor.querySelectorAll(Attributes.lottie.selector).asList()
    lotties.forEach {
        initLottie(it as HTMLElement)
    }
}

fun initLottie(element: HTMLElement) {
    val path = element.getAttribute(Attributes.lottie.value) ?: return
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