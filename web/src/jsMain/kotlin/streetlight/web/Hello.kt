package streetlight.web

import react.FC
import react.Props
import react.RefCallback
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.useRef
import react.useState
import web.html.HTMLElement

external interface HelloProps : Props {
    var name: String
}


val Hello = FC<HelloProps> { props ->
    val elementRef = useRef<HTMLElement>(null)

    div {
        +"Ahoy, ${props.name}!"

        ref = RefCallback<HTMLElement> {
            elementRef.current = it
            console.log(it)
        }

        Counter()
    }
}

val Counter = FC {
    var n by useState(0)

    div {
        +"Count: $n "
        button {
            +"Increase"
            onClick = { n += 1 }
        }
    }
}
