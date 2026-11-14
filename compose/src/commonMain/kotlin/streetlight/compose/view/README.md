## Antipatterns

### Counterparts named in KDoc

A KDoc that names the declaration's counterpart in another module, which tells a caller nothing about the declaration and goes stale when the counterpart is renamed.

```kt
/**
 * The view of [MediaForgeRoute], in a column scrolled by [scrollState] under the top bar: the name of the [galaxy] a
 * media post is posted to, if any, over the form that creates it. The cousin of the web's `mediaForgeScreen`.
 */
```

The KDoc describes the declaration alone. A counterpart is named in the package document, where the part is introduced.
