package streetlight.web.layout

import kampfire.api.Markdown
import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import koala.component.*
import koala.markdown.*

/**
 * The [description] and [links] of an entity, the links beside the description when its container has room and
 * above it otherwise.
 */
fun FlowContent.entityBody(
    description: Markdown?,
    links: List<ExtraLink>?,
    mod: Modifier? = null,
) {
    if (description == null && links == null) return
    div(modify(mod, EntityBodyStyle.Base, Padding(2), Gap(2))) {
        description?.let {
            markdown(it, EntityBodyStyle.Description)
        }
        if (links != null) {
            entityLinks(links, EntityBodyStyle.Links)
        }
    }
}

object EntityBodyStyle {
    val Base = Class("entity-body")
    val Description = Base.withBemElement("description")
    val Links = Base.withBemElement("links")
}

// language="CSS"
val EntityBodyCss get() = with(EntityBodyStyle) { """
$Base {
    display: grid;
    grid-template-rows: min-content auto;
    grid-template-areas: "links" "description";

    @container (min-width: 640px) {
        grid-template-rows: none;
        grid-template-columns: 1fr min-content;
        grid-template-areas: "description links";

        $Links { grid-template-columns: max-content; }
    }
}

$Description { grid-area: description; }

$Links {
    grid-area: links;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(var(--unit-16), 1fr));
    justify-items: center;
    align-content: start;
    gap: var(--unit);

    > * {
        width: 100%;
        max-width: var(--unit-32);
    }
}
""" }
