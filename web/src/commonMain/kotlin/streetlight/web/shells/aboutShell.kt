package streetlight.web.shells

import koala.css.Padding4
import koala.css.Shrinkable
import koala.css.ZenBg
import koala.css.modify
import koala.html.card
import koala.html.column
import koala.html.filigree
import koala.html.heading1
import koala.html.markdown
import koala.html.section
import koala.html.tab
import koala.html.tabs
import kotlinx.html.FlowContent
import streetlight.web.pages.appFooter

fun FlowContent.aboutShell() {
    column(PrivacyPolicyKey.id) {
        section {
            filigree {
                heading1("About Streetlight", modify(Shrinkable))
            }
        }

        tabs {
            tab("Background") {

                card(modify(ZenBg, Padding4)) {
                    markdown(AboutLuke)
                }
            }
            tab("Ideas") {

                card(modify(ZenBg, Padding4)) {
                    markdown(IdeasContent)
                }
            }
            tab("Roadmap") {

                card(modify(ZenBg, Padding4)) {
                    markdown("yer roadmap")
                }
            }
        }

        appFooter("web/src/commonMain/kotlin/streetlight/web/shells/aboutShell.kt")
    }
}

// language="MD"
val AboutLuke get() = """

Streetlight ![Photo by Ryan Kemp](/www/img/pearl-street-guitar.jpg) began as a side project to help out with my other side project, playing guitar out on the street corner. 

Street performance can be a strange mix of intensely social and intensely isolating. Like many of my software engineering peers, I am not blessed with a level of charisma that lets me strike up an easy conversation with people walking by. But sometimes magic happens, and someone stops to talk or even sing a song with me, and it's always the best part of my day. 

I want a way to let these interactions happen more naturally, so I write a little app called Streetlight. People can scan a QR code, and it will pull up a website where they can find my music, request songs, or sign up for a duet. 

The app works beautifully for its simple purpose, but I'm not a particularly disciplined developer, and I am cursed with no natural defense against feature creep. Other street performers express interest in using the app, so I add a way to create an account and set up shows. I want a way to easily see all the street performers in the area, so I add a map. I go to a lot of open mics, a unique opportunity for musicians to connect, so I add a way to post events.
 
At this point, I realize I am just a few database tables away from making the social media app I always wanted, so I get to work.

### All my hats---
Like other people in my age bracket, I'm a little disappointed with how the internet evolved. I grew up during a wildly optimistic time that gave us sites like Wikipedia, flickr, Craigslist, and Reddit. Suddenly, it's possible to connect with complete strangers either directly or through their posts and feel understood on a level that can be elusive offline. The internet was never perfect, but at the time it seemed to have a trajectory toward something truly great.
  
I have a background in Psychology. After graduating, I worked for several years as a research assistant, and later entered a graduate program in clinical psychology. In the therapist's chair I gained a better understanding of human creatures, our needs and our challenges. 

Over that time, the internet has shaped our world profoundly, and not clearly for the best. It's never been easier to find other people, and at the same time there is a society level phenomenon where people report feelings of isolation at unprecedented levels. Something seems broken, and our relationship to social media seems to be a part of it. As someone with an unusual combination of work experience, I believe I can understand at least part of the puzzle.
  
I've never been able to shake my optimism about the internet. I don't think technology was destined to drive us part, I think it happened because of the choices we made. Streetlight is an experiment in making a different set of choices, plant a different kind of seed, and to watch what grows.
"""

// language="MD"
val IdeasContent get() = """
Streetlight intends to test a few ideas. 

### Social media as a window to the world around us---
The core idea that all the rest revolve around is the potential for social media to be a window into the world around us. Social media as it currently exists seems to be the ultimate window, but the keywords here are *around us*. The window that we look through on social media is so often a world far away. It's not very surprising that we feel isolated when so much of the content of our reality stream can be things happening to someone else, a world away.

It is not an accident. On such a big planet, the odds that the most interesting part of our shared moment is the thing happening in your part of the world are not great. And social media companies are companies that make the most money when people are engaged, so the most interesting part of our shared moment is the part they want us to see, just to keep us looking.

Streetlight wants to try a different approach. If you've looked around the site, you might have seen a map or two, usually the first thing you see. A map is an ancient information technology and one of our best. It is proof that technology does not have to drive us apart, it can be a force for quite the opposite. I'm curious about a social media experience built around a map. Part of the draw of traditional social media is the fear of missing out, but it's possible that we are actually missing out on something even better, right around the corner. What if social media was built not to keep you scrolling but to give you a reason to stop?

Early on in Streetlight's development I was building a way to tag posts. I was going to use them as the basis for map layers, so you could filter a particular theme of locations/events. I was losing steam because I personally don't find tagging systems very effective. The hardest part is remembering all the tags, and knowing which one is the one everyone actually uses for a particular thing. 

Reddit ran with a much better idea. Instead of tags you have communities, and it is a much better way to look for something. It's not just a list of content that comes up but an entire subculture. A subreddit like **r/open_mics_denver** would be an incredibly awesome resource, and it might even exist. What if we had that subreddit, but it had a map so you could see at a glance which ones you could actually go to? I'd love to have a map of all the scenic train rides in the rocky mountains that is curated by people who love scenic train rides. This is the idea behind galaxies.
 
### Social media with respect for the value of attention---
I believe one lesson we are learning from diving headfirst into a world of information is the true value of human attention. It is the true commodity that companies compete for on the internet. I believe it is a problem that all of our social media giants are, at their core, advertising companies. I believe we pay a higher cost than we realize for building the internet around them. Streetlight is an experiment in social media that wants to understand and respect the value of human attention. 

I've always been a user interface nerd. I can spend an unreasonable chunk of time finding just the right amount of margin between two elements, I might lose an entire night of sleep. I love user interfaces that are simple and try to speak through their design. I'm curious about social media that wants to make sure that everything you are looking at is actually something you came to see and isn't too concerned with how long you stay. I recognize that I'm not alone in that vision, and I've been inspired by many projects.

We all have that person in our lives that doesn't try to keep the spotlight, and in doing so they become a light around which it is possible to feel seen. Streetlight wants to be the social media equivalent of that person.

### Streetlight's approach to community investment---
Social media platforms are all built at least twice, first by the developers and then by the community. Some of my favorite sites weren't even completely finished before the community took over and held onto the beta label for quite some time. It is not for everyone, but it's always been interesting for me to use software before the last coat of paint dries and some of the wires are still poking out. I realize that Streetlight needs people like that.
   
Since Streetlight is open source, anyone can jump in if they see an issue as well as the fix. To make this easier, I've structured each page so that a link to its source can be found at the bottom. You are never more than two clicks away from editing the code that produces the content you see, that's just one click behind Wikipedia. I'm a psychology major who started coding to make games, and along the way I found that I enjoy coding more than playing games. I have a lot to learn. A big part of my energy for coding comes from always finding out there is a better way. If you happen to come across one of these opportunities in my code, please open an issue or make a pull request.

Streetlight uses a slightly unconventional stack, at least for the web. I experimented with popular frameworks like React, I like the ideas but I've found kotlinx.html to be so versatile. I've structured my code in a similar way: the html, css, and javascript can often be found in the same file. The UI is driven by immutable state objects that are observed with kotlin coroutines. It should also be very familiar if you are coming Compose, it was my main interest before I started working with the browser. 
 
[WIP]

"""