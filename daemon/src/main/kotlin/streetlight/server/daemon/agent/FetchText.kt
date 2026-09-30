package streetlight.server.daemon.agent

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.select.Elements
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kotlin.time.Instant

fun parseHtmlDocument(html: String, url: Url): Outcome<Document> {
    if (!html.looksLikeHtml()) return Problem("Document was not html")
    return Ok(Ksoup.parse(html, url.value))
}

private val htmlStart = Regex("""^﻿?\s*(?:<!--[\s\S]*?-->\s*)*(<!DOCTYPE\s+html|<html|<[a-zA-Z]+)""", RegexOption.IGNORE_CASE)

fun String.looksLikeHtml(): Boolean = htmlStart.containsMatchIn(this)

fun Element.tryQuery(selector: String): Outcome<Elements> {
    if (selector == ".") return Ok(Elements(this))

    return try {
        Ok(select(selector))
    } catch (e: Exception) {
        Problem("Invalid selector: $selector (${e.message})")
    }
}

/** A page's text as fetched: the url it was served from after redirects, and when. */
data class FetchText(
    val servedUrl: Url,
    val text: String,
    val fetchedAt: Instant,
    val millis: Long = 0,
)