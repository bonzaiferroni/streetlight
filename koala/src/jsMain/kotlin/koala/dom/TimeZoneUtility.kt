package koala.dom

/** The browser's time zone id, such as "America/Denver". */
fun getTimeZoneId() = js("Intl.DateTimeFormat().resolvedOptions().timeZone") as String