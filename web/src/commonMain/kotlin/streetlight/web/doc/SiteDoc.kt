package streetlight.web.doc

import koala.model.Doc
import koala.model.DocSection
import koala.model.buildDocTree
import streetlight.web.shells.AboutLuke
import streetlight.web.shells.IdeasContent
import streetlight.web.shells.InformationSharedContent
import streetlight.web.shells.PrivacyIntro
import streetlight.web.shells.UpcomingSectionsContent

object SiteDoc {
    val About = Doc("about-streetlight", "About Streetlight", "[Intro Content]")

    val Team = Doc("about-team", "Team Page", listOf(
        DocSection("Luke", AboutLuke)
    ))

    val Ideas = Doc("about-ideas", "Theory", IdeasContent)

    val Roadmap = Doc("about-roadmap", "Roadmap Page", "[information]")

    val Privacy = Doc("privacy-policy", "Privacy on Streetlight", listOf(
        DocSection(null, PrivacyIntro),
        DocSection("Information shared on Streetlight", InformationSharedContent),
        DocSection("Upcoming Sections", UpcomingSectionsContent),
    ))
}

val SiteDocTree by lazy {
    buildDocTree {
        add(SiteDoc.About) {
            add(SiteDoc.Team)
            add(SiteDoc.Ideas)
            add(SiteDoc.Roadmap)
        }
        add(SiteDoc.Privacy)
    }
}