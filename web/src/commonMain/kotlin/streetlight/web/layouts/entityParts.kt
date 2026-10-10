package streetlight.web.layouts

import koala.html.*
import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.CuratorStatus
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef
import streetlight.model.data.ExtraLink
import streetlight.web.ui.curatorBadge

/**
 * The image of [entity], with [featureMod] on the image itself. It opens the [EntityDialog] for [opener], and links
 * to the entity's page when [opener] is `null`.
 */
fun FlowContent.entityImage(
    entity: Entity,
    mod: Modifier? = null,
    featureMod: Modifier? = null,
    opener: EntityRef? = entity.toEntityRef(),
) {
    when (opener) {
        null -> navigationIfNotNull(entity.toRoute(), mod) {
            containImage(entity.image, featureMod)
        }
        else -> div(mod) {
            isDialogEntity(opener)
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

/** The body of [entity], cut at [limit] when given. It opens the [EntityDialog] for [opener] when given. */
fun FlowContent.entitySummary(
    entity: Entity,
    mod: Modifier? = null,
    limit: Int? = null,
    opener: EntityRef? = entity.toEntityRef(),
) {
    markdown(entity.body, mod, limit) {
        opener?.let { isDialogEntity(it) }
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

/** A button for each of [links], and an edit button to [editRoute] when given. */
fun FlowContent.entityLinks(
    links: List<ExtraLink>?,
    editRoute: AppRoute? = null,
    mod: Modifier? = null,
) {
    div(mod) {
        links?.forEach { link ->
            btn(link.label, link.url, Zen)
        }
        editRoute?.let {
            btn("edit", it, Zen)
        }
    }
}
