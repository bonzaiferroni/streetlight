package streetlight.web.layout

import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.CuratorStatus
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef
import streetlight.model.data.ExtraLink
import streetlight.web.component.curatorBadge
import koala.component.*
import koala.layout.*
import koala.markdown.*
import streetlight.web.model.flair
import streetlight.web.model.toEntityRef
import streetlight.web.model.toFlair
import streetlight.web.model.toRoute

/**
 * The image of [entity], with [featureMod] on the image itself. It opens the [EntityDialog] for [ref], and links
 * to the entity's page when [ref] is `null`.
 */
fun FlowContent.entityImage(
    entity: Entity,
    mod: Modifier? = null,
    featureMod: Modifier? = null,
    ref: EntityRef? = entity.toEntityRef(),
) {
    when (ref) {
        null -> navigationIfNotNull(entity.toRoute(), mod) {
            containImage(entity.image, featureMod)
        }
        else -> div(mod) {
            setEntityRef(ref)
            containImage(entity.image, featureMod)
        }
    }
}

/** The heading of [entity], linking to its source when it has one and to its page otherwise. */
fun FlowContent.entityHeading(entity: Entity, mod: Modifier? = null) {
    navigationIfNotNull(entity.url?.value ?: entity.toRoute()?.toRelativePath(), mod) {
        heading5(entity.label, modify(LineHeight115, SingleLine, Bold))
    }
}

/** The body of [entity], cut at [limit] when given. It opens the [EntityDialog] for [ref] when given. */
fun FlowContent.entitySummary(
    entity: Entity,
    mod: Modifier? = null,
    limit: Int? = null,
    ref: EntityRef? = entity.toEntityRef(),
) {
    markdown(entity.body, mod, limit) {
        ref?.let { setEntityRef(it) }
    }
}

/** The flair of [entity] with [flairMod], or the marks of [curator] in its place. */
fun FlowContent.entityBadge(
    entity: Entity,
    curator: CuratorStatus? = null,
    mod: Modifier? = null,
    flairMod: Modifier? = null,
) {
    div(mod) {
        when (curator) {
            null -> magicIcon(entity.toFlair(), modify(flairMod, ColorSchemeFg, OpacityLow))
            else -> curatorBadge(curator)
        }
    }
}

/** A button for each of [links]. */
fun FlowContent.entityLinks(
    links: List<ExtraLink>?,
    mod: Modifier? = null,
) {
    div(mod) {
        links?.forEach { link ->
            btn(link.label, link.url, Zen)
        }
    }
}
