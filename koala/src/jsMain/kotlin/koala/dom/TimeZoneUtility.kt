package koala.dom

fun getTimeZoneId() = js("Intl.DateTimeFormat().resolvedOptions().timeZone") as String