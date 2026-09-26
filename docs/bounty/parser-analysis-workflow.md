# Parser Analysis Workflow

Each build of the parse pipeline is judged against a source of truth that Leadline gathers independently with Playwright. Comparing the two names the point of failure for every location that does not yield its data, and those findings shape the next build.

The Playwright probe that found the Bar 404 and Roxy failures (late rendering, iframe content) is the model for step 2.

## Workflow

1. **Gather.** Run the new-build reset from the daemon package document, raise `parserBuildId`, and let the daemon complete a full check, writing `logs/parser/<build>/`.
2. **Establish the truth.** For each location, Leadline renders the feed and up to 5 of its event pages with Playwright through the daemon's own jars, with no resource blocking, a settle wait after load, and iframe contents read. From the render, record what a person sees: the events listed, and for each sampled page its title, date, start time and whether it has a description.
3. **Compare.** Set the truth beside the location's `ParseReport`, its saved html and `-trim.html`, and its schemas. Walk the stages in order and name the first stage where the pipeline's result departs from the truth.
4. **Report.** Write one file per location to `logs/LLParse/<build>/<origin>.md`, and a build summary to `logs/LLParse/<build>/summary.md` ranking the points of failure by the events they cost.
5. **Apply.** Propose the changes for the next build as a bounty, smallest set of changes that addresses the largest losses first. Record any convention decided along the way in its package document.

## Points of Failure

A location lists as many as are relevant, but the smallest set that is sufficient to explain its losses.

| Point | Example |
|---|---|
| Feed address | The location's events url is not its events page (the Roxy's is its home page) |
| robots.txt | Disallowed; a no, not a failure |
| HTTP request | 4xx, 5xx, a bot wall (Ticketmaster 401, Etix 202), a redirect off-site |
| JS rendering | Content arrives after load, or never without scripting; fetch mode not escalated |
| Frames | Content inside an iframe |
| HTML trimming | Content removed or cut by the cap before the LM saw it |
| LM capability | Instructions clear, content present, answer wrong |
| LM instructions | The answer follows the instructions, and the instructions led it astray |
| Schema structure | The schema cannot express what the page holds (a date split across elements) |
| Schema validation | A good schema rejected, or a bad one accepted |
| Schema application | The right selector yields the wrong element (the first-match info block) |
| Link resolution | Event links point to ticket vendors or resolve to the wrong page |
| Event building | Date or time text that does not parse, a wrong time, a missing start time |
| Event filtering | Dropped as past or as a duplicate when it should not be |
| Strikes | An origin benched when it should not have been |

## Location Report

```markdown
# <origin>

Build: <build>
Truth: <n> upcoming events on the feed, <n> event pages sampled
Pipeline: <found> found, <created> created, <past> past, <unparsed> unparsed

## Points of Failure
* <point>: <what happened, with the evidence: selector, text, status, file>

## Lessons
* <what would have caught or fixed it, and at which stage>
```

Bounty:
* Write a Playwright probe usable from `jshell`, or as a small script in the daemon's test source set, that renders a url and records its visible text, iframes and events as step 2 describes
* Run steps 1 to 4 for V3
* Draw up the V4 bounty from the V3 summary
* Record the workflow in the Workflows section of `docs/packages/streetlight.server.daemon.md` once it has run once
