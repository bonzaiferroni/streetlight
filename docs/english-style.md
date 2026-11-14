# English Style

Guidelines for the English on the site and in the documentation, for readers who are people and agents alike.

## Voice

A sentence names who acts. Where the site or the crew acts, the subject is "we", and the verb is active: "We built Streetlight to…", not "Streetlight was designed to…".

## Substance

A sentence states its subject, not that the page will cover it. Commentary about the writing itself, such as "this page describes in detail", is left out: "Here is what we collect, why we collect it, and what you can control", not "This page describes in detail what you can expect".

## Antipatterns

### Avoid documentation that adds no meaning beyond its identifiers

A description that restates the name it describes tells the reader nothing the name did not.

```md
| [`kotlin-style.md`](docs/kotlin-style.md) | The style of Kotlin code. |
```

Better: say what the reader finds there, as "Project guidelines and specifications for writing Kotlin."
