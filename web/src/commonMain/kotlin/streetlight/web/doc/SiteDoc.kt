package streetlight.web.doc

import kampfire.api.toMarkdown
import koala.SiteImage
import koala.model.Doc
import koala.model.DocSection
import koala.model.docTreeOf
import koala.model.toTable
import streetlight.web.shells.AboutLuke
import streetlight.web.shells.IdeasContent
import streetlight.web.shells.InformationSharedContent
import streetlight.web.shells.PrivacyIntro
import streetlight.web.shells.UpcomingSectionsContent

object SiteDoc {
    val About = Doc("about-streetlight", "About Streetlight", "[Intro Content]".toMarkdown())

    val Team = Doc("about-team", "The Team", listOf(
        DocSection("Luke", AboutLuke)
    ))

    val Ideas = Doc("about-ideas", "Theory", IdeasContent)

    val Roadmap = Doc("about-roadmap", "Roadmap Page", "[information]".toMarkdown())

    val Privacy = Doc(
        docId = "privacy-policy", title = "Privacy on Streetlight",
        sections = listOf(
            DocSection(null, PrivacyIntro),
            DocSection("Information shared on Streetlight", InformationSharedContent),
            DocSection("Upcoming Sections", UpcomingSectionsContent),
        ),
        image = SiteImage.crossing.url
    )
}

val SiteDocTree by lazy {
    docTreeOf {
        add(SiteDoc.About) {
            add(SiteDoc.Team)
            add(SiteDoc.Ideas)
            add(SiteDoc.Roadmap)
        }
        add(SiteDoc.Privacy)
    }
}

val SiteDocTable by lazy {
    SiteDocTree.toTable()
}