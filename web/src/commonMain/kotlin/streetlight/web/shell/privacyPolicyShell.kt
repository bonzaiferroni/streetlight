@file:Suppress("MarkdownIncorrectTableFormatting")

package streetlight.web.shell

import kampfire.api.toMarkdown
import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.web.layout.mainBody
import streetlight.model.ui.FrontDeskRoute
import koala.component.*
import koala.markdown.*
import koala.page.*

fun FlowContent.privacyPolicyShell() {
    mainBody("privacyPolicyShell.kt") {
        column(PrivacyPolicyKey.id, Gap(8)) {
            section {
                filigree {
                    heading1("Privacy on Streetlight", Shrinkable)
                }
                card(modify(ZenBg, Padding(4))) {
                    markdown(PrivacyIntro)
                }
            }

            // td: table of contents

            section {
                filigree {
                    heading2("Information shared on Streetlight")
                }
                card(modify(ZenBg, Padding(4))) {
                    markdown(InformationSharedContent)
                }
            }

            section {
                filigree {
                    heading2("Upcoming sections")
                }
                card(modify(ZenBg, Padding(4))) {
                    markdown(UpcomingSectionsContent)
                }
            }
        }
    }
}

object PrivacyPolicyKey {
    val id = Id("privacy-policy")
}

// language="MD"
val PrivacyIntro get() = """

We built Streetlight to have a high level of transparency and control over the information you share. Here is what we collect, why we collect it, and what you can control.

### In a nutshell---
* We aim to collect only the information the site's features need. It is possible to use many features without creating an account. !["I need my privacy"](/www/lottie/astronaut-reading.json) 
* We do not sell or give information to third parties beyond the functionality of site features and a lawful subpoena by a recognized authority. 
* We do not show ads or share information with advertisers.
* We provide functionality to help you keep track of what information you share and easily remove it.
* Streetlight is open source and can be inspected to verify an effective implementation of these policies. 

#### Important limitations 
Sharing information on the internet carries risk that is difficult to completely safeguard against. For example, it is possible for third parties to access websites and gather public information on individuals and the community.  

Streetlight is at an early stage of development, and you may run into bugs or missing features. We'd appreciate hearing about any problems or suggestions on the [Help & Feedback](${FrontDeskRoute.toRelativePath()}) page.

""".toMarkdown()

// language="MD"
val InformationSharedContent get() = """

Streetlight does not require you to create an account to see information shared publicly. To share information or interact, you may create an account as a guest or registered user.

#### Using Streetlight without an account---

We keep the core functionality of Streetlight available to users without an account. It is possible to browse the majority of the content without ever signing in.

We are experimenting with the ability for signed-out users to share posts. These posts are reviewed by a moderator before they become visible to the community at large to ensure the content fits the theme of the galaxy and the site's content policies. A galaxy is a streetlight community with a particular content focus. The moderator is a community member too, the galaxy creator or one of its members. 

Creating an **event post** involves sharing the following information:

|   |   |
|---:|:---|
| *required* | The name of the event, location, cost, and the start time |
| *optional* | The event website and additional links |
| *optional* | A description of the event |
| *optional* | A feature image |

Creating a **location post** involves sharing the following information:

|   |   |
|---:|:---|
| *required* | The name, city, address, and geolocation |
| *optional* | The location website and additional links |
| *optional* | A description of the location |
| *optional* | A feature image |

There are a few important considerations when sharing this information. 
* A post shared by a signed-out user cannot be directly removed by that user, since there is no way to verify they are the same user. We make sure the user is aware of this limitation. We are working on a feature that lets signed-out users send a special request for the removal of a post.
* **Geolocation** is among the most sensitive information that it is possible to share with Streetlight. For this reason, all users are advised not to share a personal residence as a location. Ultimately, this decision is left to the discretion of the user.
* It is possible to set a **feature image** by providing a web address for the event or location, the image itself does not need to be provided by the user. The server checks the metainformation of the web content for an image specifically intended to be shared on social media. When an image is gathered this way, it is not associated with the user account that submitted the post.  

#### Creating a Streetlight account---

Creating an account enables additional site functionality. Users with an account can create their own galaxies and can submit posts directly. Their information is also synchronized across devices.

Creating an account involves sharing the following information:

|   |   |
|---:|:---|
| *required* | A **username** |
| *required* | A **password** |
| *optional* | Eventually users will be able to share an **email address** |

These credentials allow a user to sign in to the site. Users are strongly encouraged to pick a unique and strong password with a variety of character types, and a minimum level of complexity is required. All traffic on Streetlight is encrypted and transferred securely using the https protocol. When the server receives this information, it does not store the password as submitted, it transforms the password into a string of characters known as a hash. When the user signs in with their password, this same transformation is applied to verify their credentials. 

Streetlight uses your **email address** to reset your password and as an additional layer of authentication. They may also be used to receive news and messages from Streetlight, strictly on an opt-in basis. This functionality is not yet available on the site, and at this early stage of development we do not gather email addresses. Keep that password safe!

""".toMarkdown()

// language="MD"
val UpcomingSectionsContent get() = """
This document will always be a work in progress, but at the moment there are important considerations not yet described here. At a minimum, they include the following:

* A description of features that allow you to track the information you've shared and remove it.
* The circumstances under which Streetlight will provide or remove information in response to a lawful subpoena by a recognized authority.
* Information observable by third parties as a part of site functionality.

```
this is a code test!
val = 20
```
""".toMarkdown()

