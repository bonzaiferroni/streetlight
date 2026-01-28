import kotlinx.browser.document
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.w3c.dom.HTMLIFrameElement
import streetlight.web.pages.homePage

fun main() {
    console.log("injecting document ")
    val htmlText = buildString {
        append("<!DOCTYPE html>")
        appendHTML().html {
            homePage()
        }
    }
//    console.log(documentText)
    document.open()
    document.write(htmlText)
    document.close()
//    val iframe = document.createElement("iframe") as HTMLIFrameElement;
//    iframe.srcdoc = htmlText;
//    document.body?.appendChild(iframe);
}