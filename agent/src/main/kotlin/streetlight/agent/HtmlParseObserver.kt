package streetlight.agent

/** Receives what one [HtmlParserClient.readHtml] call does, as it happens. */
interface HtmlParseObserver {

    /** The page was trimmed to [result], and [promptHtml] is the html exactly as the model will read it. */
    fun trimmed(result: TrimResult, promptHtml: String) {}

    /** The [model] answered with [json] on attempt [attempts], taking [millis]. */
    fun responded(model: String, json: String, inputTokens: Int?, outputTokens: Int?, millis: Long, attempts: Int) {}
}
