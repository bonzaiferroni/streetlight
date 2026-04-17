@file:Suppress("MarkdownIncorrectTableFormatting")

package streetlight.web.shells

import koala.css.*
import koala.html.*
import koala.markdown.markdownBlocksOf
import kotlinx.html.FlowContent
import streetlight.web.pages.appFooter

fun FlowContent.privacyPolicyShell() {
    column(PrivacyPolicyKey.id, modify(Gap8)) {
        section {
            filigree {
                heading1("Privacy on Streetlight", modify(Shrinkable))
            }
            card(modify(ZenCardBg, Padding4)) {
                markdown(PrivacyIntro)
            }
        }

        // td: table of contents

        section {
            filigree {
                heading2("Information shared on Streetlight")
            }
            card(modify(ZenCardBg, Padding4)) {
                markdown(InformationSharedContent)
            }
        }

        section {
            filigree {
                heading2("Upcoming sections")
            }
            card(modify(ZenCardBg, Padding4)) {
                markdown(UpcomingSectionsContent)
            }
        }

        appFooter("web/src/commonMain/kotlin/streetlight/web/shells/privacyPolicyShell.kt")
    }
}

object PrivacyPolicyKey {
    val id = Id("privacy-policy")
}

// language="MD"
val PrivacyIntro get() = """

Streetlight was designed to provide a high level of transparency and control over the information you share.
This page describes in detail what you can expect when using the site.

### In a nutshell---
* We !["I need my privacy"](/www/lottie/astronaut-reading.json) gather only the information necessary to provide the features you see on the site. It is possible to use many features without creating an account.
* We do not sell or give information to third parties beyond the functionality of site features and a lawful subpoena by a recognized authority. 
* We do not show ads or provide information to advertisers.
* We provide tools that let you keep track of what information you share and easily remove it.
* Streetlight's source code is open and can be inspected to verify an effective implementation of these policies.

#### Important limitations 
Sharing information on the internet can carry risk that is difficult to completely safeguard against. For example, it is possible for third parties to access websites and gather public information on individuals and the community. It is important for each user to be mindful of what they share and how it may be used outside of Streetlight's control. 

Streetlight is at an early stage of development, and certain privacy features are not yet available. For example, we plan to provide a way for you to download all of your data or remove it entirely from the site. In the long term, we believe that Streetlight's open source will be an important reason to trust the integrity of the site. In the short term, it may carry additional risk, since potential attackers can also inspect the code for vulnerabilities.

"""

// language="MD"
val InformationSharedContent get() = """

This section describes the information that can be shared with Streetlight, how it is stored, and how it is used. We'll follow the basic progression that most users will follow, from using the site without an account, to account creation and beyond. 

### Using Streetlight without an account---

We keep the core functionality of Streetlight available to users without an account. It is possible to browse the majority of the content without ever signing in. 

Streetlight lets users mark posts that they are interested in. While you are not signed in, the events that you mark are stored on your device, and no information is sent to the server. You may see the number of lights increase on the post, but this change is not visible to other users.

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

### Creating a Streetlight account---

Creating an account enables additional site functionality. Users with an account can create their own galaxies and can submit posts directly. Their information is also synchronized across devices.

Creating an account involves sharing the following information:

|   |   |
|---:|:---|
| *required* | A **username** |
| *required* | A **password** |
| *optional* | Eventually users will be able to share an **email address** |

These credentials allow a user to sign in to the site. Users are strongly encouraged to pick a unique and strong password with a variety of character types, and a minimum level of complexity is required. All traffic on Streetlight is encrypted and transferred securely using the https protocol. When the server receives this information, it does not store the password as submitted, it transforms the password into a string of characters known as a hash. When the user signs in with their password, this same transformation is applied to verify their credentials. 

Streetlight uses your **email address** to reset your password and as an additional layer of authentication. They may also be used to receive news and messages from Streetlight, strictly on an opt-in basis. This functionality is not yet available on the site, and at this early stage of development we do not gather email addresses. Keep that password safe!

"""

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
"""

fun String.toMarkdown() = markdownBlocksOf(this)