# Crawler Work

The workflow for tuning the crawler build by build: stage the records that want another read, run the crawler, read what it reports, check the pages by hand, and carry the lessons into the next build. It was worked out over builds V0 to V30 of the parser (2026-09-24 to 2026-09-29). Read the crawler and agent package documents (`docs/packages/streetlight.server.daemon.crawler.md`, `…agent.md`) before changing either package.

## Where It Stands

* Build **V39** (2026-10-04): a stored feed schema is reused only when its `link`, when it has one, matches among its events. V38's stored Eventbrite feed schema kept matching its events while its hashed link class had changed, so 61 of 65 feeds followed no event pages and every record was dropped for want of a date. The stage nulls `checked_at` on the 61 Eventbrite leads. Dumps `logs/schema/lead-before-V39.json`.
* The **Luma Denver** (2026-10-03) and **AllEvents Denver** leads are added, then 56 more AllEvents towns (see AllEvents Radius), with Denver's geo point. Luma's first read yielded nothing: its cards carry no date or link (see Open Leads). AllEvents (`Crawl-delay: 10`) holds dates in its Basic text and declares 96 events in JSON-LD.
* Build **V38** (staged after the tag backfill): events carry tags from the per-tag regression. The stage deletes Studio@Mainstreet's events, links and the `tockify.com` parser, and nulls every `checked_feed_at` and lead `checked_at`, keeping links and the other events: known pages are skipped and pages deferred past the 30-page limit are read, so the counts of created and known events show what V37's first pass left. Dumps `logs/schema/*-before-V38.json`.
* Build **V37** is staged as a full rescan for classification (2026-10-02): every event, link and link alias deleted, every location's `checked_feed_at` and every lead's `checked_at` nulled. Parsers and location website reads (`location.checked_at`) kept. Each classified event writes its check's report. Dumps `logs/schema/*-before-V37.json`.
* Since V37 started: titles are capped at 150 characters (`isPlausibleTitle`), after a `tockify.com` page schema took the description as the title for Studio@Mainstreet (8 events with paragraph titles). Leads are read in the order of their random ids, so Meetup and Eventbrite interleave. The 9 Meetup leads checked before a mid-run stop were unchecked. The V38 bump stages Studio@Mainstreet: its events, links, and the `tockify.com` parser.
* Build **V35** is staged: the Grimm Brothers feed, and the new Eventbrite Denver lead. V34 read all 192 location websites: 623 events and 43 locations created since 09:00 on 2026-10-01, 89 locations filled, locations with a feed from 67 to 124. Dumps `logs/schema/*-before-V35.json`.
* The crawler runs leads concurrently: up to `maxWorkers` (8) at once, taking more each second as room opens. Robots and OSM gates space requests, the LM takes one request at a time, and database writes run one at a time through `Crawler.dbWrite`.
* JSON-LD supplements the LM, never replaces it: every lead is read by its schema, and the values its page declares are laid over the read at resolution time. Meta-only values (such as `og:site_name`) never outrank the LM.

## Meetup Radius

Every Colorado town with a Meetup city feed (`/find/us--co--<town>/`) is added as an `EventFeed` lead, ring by ring outward from Denver (39.7392, -104.9903). A town without a feed answers 404.

* **Covered to 84 km** (Estes Park), for the towns probed. 50 feeds: Denver, Aurora, Arvada, Westminster, Littleton, Golden, Parker, Boulder, Castle Rock, Longmont, Fort Collins, Colorado Springs (the first twelve, chosen before the radius); to 29.2 km, Wheat Ridge, Commerce City, Englewood, Thornton, Morrison, Broomfield, Lone Tree, Indian Hills, Kittredge, Louisville; then Lafayette, Evergreen, Brighton, Erie, Conifer, Dacono, Frederick, Fort Lupton, Firestone, Black Hawk, Idaho Springs, Bennett, Nederland, Elizabeth, Mead, Bailey, Lyons, Georgetown, Berthoud, Johnstown, Monument, Loveland, Evans, Greeley, Windsor, Woodland Park, Estes Park, and Fort Morgan (116 km, beyond the radius).
* **No feed** inside the radius: Lakewood, Glendale, Edgewater, Sheridan, Federal Heights, Greenwood Village, Northglenn, Centennial, Highlands Ranch, Ken Caryl, Superior, Cherry Hills Village, Columbine, Foxfield, Mountain View, Lakeside, Bow Mar, Welby, Castle Pines.
* **Complete to 29.2 km.** Beyond it, the 84 km ring holds only the towns probed on 2026-10-01; the next sweep fills it in (such as Platteville, Hudson, Keenesburg, Kiowa, Pine, Central City, Empire, Wellington, Timnath, Severance, Eaton, Palmer Lake) and reaches outward.
* Suburban feeds mostly repeat Denver's events (Commerce City: 48 of 56 in Denver); the duplicate check absorbs them. Mountain and plains towns hold few events of their own (Bennett 0, Georgetown 1).

## Eventbrite Radius

Eventbrite's city feeds (`/d/co--<town>/all-events/`) answer for any real town, each with about 21 events near it; a town it does not know answers 404. Every town of the Meetup radius has a lead, and those Meetup lacks a page for (Lakewood, Centennial, Highlands Ranch, Northglenn, Greenwood Village, Superior, Castle Pines, Edgewater, Sheridan, Federal Heights, Ken Caryl): 61 feeds with Denver. Glendale is left out, its page holding Boulder's events. Within about 20 km a town's page mostly repeats Denver's; from about 22 km out most of its events are its own.

## AllEvents Radius

AllEvents' city feeds are `allevents.in/<town>`, or `allevents.in/<town>-co` when the name belongs to a town elsewhere (Louisville answers for Kentucky, Brighton for England). A slug it does not know redirects to `/events`. Every town of the Meetup and Eventbrite radius was probed (2026-10-03, `Crawl-delay: 10`): 56 feeds with events have leads, 57 with Denver. Each takes its town's geo point from its Eventbrite or Meetup lead; Cherry Hills Village, Columbine, Foxfield and Glendale, which have neither, take theirs from OSM.

* **No events:** Indian Hills, Dacono, Federal Heights, Bow Mar, Welby, Black Hawk.
* **No feed:** Lone Tree, Sheridan, Mountain View, Lakeside, Castle Pines.

## Principles

These hold for every change a build makes. The General Model has its single home in `docs/packages/streetlight.server.daemon.crawler.md`.

| Principle | Meaning |
|---|---|
| General Model | A change is made only when its mechanism holds for any site and its need is seen across many origins. A site that doesn't fit stays unread |
| No data over bad data | Nothing is guessed. An event without a title or a parsed start ahead is dropped |
| Respect layer | robots.txt, the Streetlight user agent, pacing (`Crawl-delay`, or 1 s per origin without one; a `429` or `503` is retried once after 30 s), strikes, the page limit, and `ParseMode` on descriptions |
| Model-agnostic | A mistake of the LM is caught by validation, never answered with an instruction written for one model |
| Standard measures | Scripted fetches settle with ordinary means (wheel scroll, steady text), never site tricks |
| Low-hanging fruit | The fix that lifts many origins comes before the one that rescues a single site |

## The Cycle

1. **Build.** Raise `parserBuildId` in `ParseReport.kt` (e.g. `V10` → `V11`) whenever any stage changes. The navigator builds and runs; the agent does not compile.
2. **Stage.** Reset only the records that want another read (below). Tables are never cleared wholesale unless the navigator asks for a fresh rescan. The daemon must be stopped first: a running daemon on the old build consumes the staged leads (check with `pgrep -af daemon`).
3. **Run.** The navigator runs the crawler until the staged leads have been read.
4. **Analyze.** Read `logs/parser/Vn/*.json`, the saved html beside them, and the events created in the run's window.
5. **Probe.** Check what the page really holds with ksoup or Playwright in jshell. The probe is the source of truth, not the report and not the LM.
6. **Lessons.** Sort each finding into a gremlin (the code is wrong), a reef (the domain, not yet handled), or accepted/unread by rule. Only lessons that pass the General Model become the next build.

## Environment

| Piece | Value |
|---|---|
| LM | Ollama with `qwen3.5-parser` (`FROM qwen3.5:9b`, `num_predict 1024`), Modelfile in `~/apps/ollama/`, on an RTX 3060 12GB |
| `.env` | `LM_PROVIDER=ollama`, `LM_MODEL=qwen3.5-parser`, `LM_CONTEXT_LENGTH=131072`, and `DB_URL`/`DB_USER`/`DB_PASSWORD` |
| Database | Dev Postgres. Timestamps are stored in local time (Mountain), not UTC |

The psql connection from the project root:

```bash
set -a; . ./.env; set +a; U=${DB_URL#jdbc:postgresql://}; H=${U%%/*}; D=${U#*/}; D=${D%%\?*}
export PGHOST=${H%%:*} PGPORT=${H##*:} PGUSER=$DB_USER PGDATABASE=$D PGPASSWORD=$DB_PASSWORD
```

Enums are stored by ordinal:

| Column | Values |
|---|---|
| `link.access` | 0 Granted, 1 RobotsBlock, 2 Refused |
| `link.content` | 0 Schema, 1 OffSchema, 2 OffScope, 3 Unknown, 4 Unread |
| `link.parse_outcome` | 0 Complete, 1 Partial, 2 Fail |
| `parser.schema_type` | 0 EventFeed, 1 EventPage |
| `lead.lead_type` | 0 EventPage (never stored), 1 EventFeed, 2 Location, 3 Event, 4 EventScan |
| `location.parse_mode` | 0 None, 1 Partial, 2 Full |

A `lead` row needs its `id` given (`gen_random_uuid()`); a general feed or scan also needs `name`, `geo_point` and `timezone_id`. An `EventFeed` lead is read again each day; every other lead type is read once, when its `checked_at` is null.

## Staging

Before each stage, dump what will change to `logs/schema/`, named `<table>-before-Vn.json`:

```bash
psql -At -c "select coalesce(json_agg(k), '[]') from link k" > logs/schema/link-before-Vn.json
psql -At -c "select coalesce(json_agg(p order by p.created_at), '[]') from parser p" > logs/schema/parser-before-Vn.json
```

**The retry rule.** A location is staged for another read when its feed link is not a clean read (content not Schema, or outcome not Complete), when any granted link on its origin is not a clean read, or when it has no events at all. Staging a location nulls `checked_feed_at`, deletes its feed link and its origin's parsers, and resets the outcome of its `Unread` links. Run it in one transaction (`psql -q -1 -v ON_ERROR_STOP=1 -f stage.sql`):

```sql
create temp table feeds as
    select l.id as location_id, l.slug, k.id as link_id, k.origin_id, k.content, k.parse_outcome
    from location l join link k on k.id = coalesce(
        (select link_id from link_alias where url = l.events_url),
        (select id from link where url = l.events_url),
        (select link_id from link_alias where url = rtrim(l.events_url, '/')),
        (select id from link where url = rtrim(l.events_url, '/')))
    where l.events_url is not null;
create temp table retry as
    select f.* from feeds f
    where coalesce(f.content, -1) <> 0 or coalesce(f.parse_outcome, -1) <> 0
       or exists (select 1 from link k where k.origin_id = f.origin_id and k.access = 0
                  and (coalesce(k.content, -1) <> 0 or coalesce(k.parse_outcome, -1) <> 0))
       or not exists (select 1 from event e where e.location_id = f.location_id);
update link set parse_outcome = null
    where origin_id in (select origin_id from retry) and content = 4;
update location set checked_feed_at = null where slug in (select slug from retry);
delete from parser where origin_id in (select origin_id from retry);
delete from link where id in (select link_id from retry);
select string_agg(slug, ', ' order by slug) from retry;
```

A feed that is never staged is not read again within a day (`checkInterval`), so a build only touches what was staged.

**A targeted stage** tests chosen sites from scratch, and is the usual stage now. Dump `event`, `link`, `link_alias` and `parser` first. Then, in one transaction: pick the feeds by slug, take their origins from `events_url` (plus any origin their event pages live on, such as `ticketsqueeze.com` for Red Rocks), delete the events at those locations or whose `url` is on those origins (so venues spawned from their pages go too), delete the origins' link aliases and links, delete their parsers only when the stored schema is the problem, and null `checked_at`. A stored lead is staged the same way through `lead.checked_at`, and a location's website read through `location.checked_at`. Deleting whole events is what lets a build's improvements show; otherwise the duplicate check keeps the old ones.

```sql
create temp table feeds as select id, slug, events_url from location where slug in ('larimer-lounge-denver', 'summit-denver');
create temp table origins as
  select distinct regexp_replace(split_part(split_part(events_url,'://',2),'/',1),'^www\.','') as origin_id from feeds;
delete from event where location_id in (select id from feeds)
   or regexp_replace(split_part(split_part(url,'://',2),'/',1),'^www\.','') in (select origin_id from origins);
delete from link_alias where link_id in (select id from link where origin_id in (select origin_id from origins));
delete from link where origin_id in (select origin_id from origins);
delete from parser where origin_id in ('summitdenver.com');
update location set checked_feed_at = null where id in (select id from feeds);
```

**Rereading an event page.** An event page is read again only when its link's `parse_outcome` is null and its `content` is null or `Unread` (`Link.wantsRead`), so staging one nulls both.

**Undoing a stray run.** When a daemon on the old build ran after a stage, delete what it made since the stage (the dump file's time, in local time): links and their aliases, parsers and events with `created_at` after it, then null `checked_feed_at` (a location's feed) or `checked_at` (a lead) on the feeds it checked.

## Reports

A check writes `logs/parser/Vn/<type>-<address>.json` only when it needs attention: it failed, a page needs work, its feed yielded nothing created, duplicate or known, a location was spawned or failed to be, or an event was classified. **No report means a clean check with nothing classified.** Beside it, `html/` holds each page that needs work as fetched (`<address>.html`) and as the LM saw it after trimming (`<address>-trim.html`). Reports are named for the lead's type and url (V23 on; earlier builds are named `<origin>.json`). A page that laid declared values over its read carries the note "Declared values from its JSON-LD".

**Comparing builds.** Reports only cover checks that needed attention, so the fairest comparison is the events themselves: from the pre-stage dump against the table after the run, per staged location, count events, average description length, shortened descriptions (`Read more`), costs and ticket links (`links` not null).

| Part | What to read |
|---|---|
| `lead` / `pages[]` | `state`, `access`, `content`, `parseOutcome`, `status`, `notes`, `fetch` (mode, chars, text chars, millis) |
| `trim` | The page's trim stats, once per page |
| `lm[]` | Each request (`schema`, `time`, `description`), its cap cut, token counts and the raw `response`: the first place to look when a schema is odd |
| `schema` | `source` (new or stored), `validation`, `dropped` fields, `eventCount`, `fieldFill` (how many events each selector filled) |
| `classifications[]` | Each event's classification on the page it was read from: `rankedTags` (the five most similar tags, each with its `similarity`), the `tags` given, and its `declaredType`. Every classified event writes its check's report |
| `records` | The counts: `found`, `created`, `past`, `unnamed`, `shortened`, `duplicates`, `known`, `createFailed`, `locationsSpawned`, `locationsFailed`, `classified`. Each record's story (unparsed dates, duplicate titles, failures, location outcomes) is a note on its page |
| `failure` | An exception that cut the check short |

The summary pass over a build:

```bash
cd logs/parser/Vn && python3 - <<'EOF'
import json, glob
from collections import Counter
for f in sorted(glob.glob('*.json')):
    d = json.load(open(f)); fd = d['lead']; e = d['records']; s = fd.get('schema') or {}
    print(f"{f[:-5][:24]:24} {fd.get('content')} {fd.get('parseOutcome')} ev={s.get('eventCount')} "
          f"pages={dict(Counter(p['state'] for p in d['pages']))} "
          f"found={e['found']} created={e['created']} past={e['past']} dup={e['duplicates']} "
          f"known={e.get('known', 0)} cls={e.get('classified', 0)} "
          f"notes={len(fd.get('notes', []))} fail={d.get('failure')!r}")
EOF
```

The events a run created, by location (the window is local time):

```sql
select l.slug, count(*) from event e join location l on l.id = e.location_id
where e.created_at > now() - interval '3 hours' group by 1 order by 2 desc;
```

Every event found lands in exactly one bucket: created, past, unnamed, unparsed, duplicate, unlocated or a failed create. Adding up the buckets for a feed shows where its events sank.

## Probes

**ksoup** reads a saved page the way the crawler does. The classpath comes from the Gradle cache:

```bash
C=~/.gradle/caches/modules-2/files-2.1
EXTRA=$(find $C/com.fleeksoft.charset $C/com.fleeksoft.io $C/co.touchlab -name "*-jvm-*.jar" ! -name "*sources*" | sort | tr '\n' ':')
CP=$(find $C/com.fleeksoft.ksoup -name "*-jvm-0.2.6.jar" ! -name "*sources*" | tr '\n' ':')$EXTRA$(find $C/org.jetbrains.kotlin/kotlin-stdlib -name "kotlin-stdlib-2.4.20.jar" | head -1):$(find $C/org.jetbrains.kotlinx -name "kotlinx-io-core-jvm-0.9.0.jar" | head -1):$(find $C/org.jetbrains.kotlinx -name "kotlinx-io-bytestring-jvm-0.9.0.jar" | head -1)
jshell --class-path "$CP" probe.jsh
```

```java
import com.fleeksoft.ksoup.Ksoup;
var html = new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of("page.html")));
var doc = Ksoup.INSTANCE.parse(html, com.fleeksoft.ksoup.parser.Parser.Companion.htmlParser(), "https://example.com/");
for (var el : doc.body().select(".event-item")) System.out.println(el.text());
/exit
```

The quickest Playwright probe is Node with the copy already installed: a script in the scratchpad that does `require('/home/starfox/projects/streetlight/benchmark/node_modules/playwright')`, run from `benchmark/`. For a saved page, abort every route and `setContent(html, { waitUntil: 'domcontentloaded' })`, or it waits on the network.

**Playwright** shows what a scripted fetch sees: the settle, lazy loading and iframes. Its classpath is the `com.microsoft.playwright` 1.61.0 jars plus `gson-2.9.0`. A probe mirrors `fetchTextWithScripting`: the Streetlight user agent, images, media and fonts blocked, then wheel scrolls of 2500px until the visible text of every frame holds steady for 3s, capped at 15s. Print the body's `innerText` length, each frame's url and text length, and the text around the dates in question.

A quick check of a saved page without a probe: strip scripts and tags with a regex, then count date and time matches in the text, to learn whether the dates are in the html at all or arrive by script.

## Build History

| Build | Change and lesson |
|---|---|
| V1–V3 | Local LM (Qwen via Ollama): required JSON schema, temperature 0.1 and a `num_predict` cap end token loops. Conservative trimming (`HtmlTrimmer`), instructions after the html |
| V4–V5 | The link model (`LinkAccess`, `LinkContent`, `ParseOutcome`), strikes (3 consecutive 4xx/5xx bench an origin), 30 pages per check, reports only when needed. The description gate dropped: a title and a start are the minimum |
| V6 | `SchemaMediator`: stored schemas first, then the LM, validated. The time follow-up (month, day, start time) when fewer than half the starts parse. `normalizeSpaces` fixed the ` ` space before PM |
| V7 | The fuzzy duplicate guard (same local day, `fuzzyMatches` at 0.8), bracket notes cut from titles, descriptions shortened to 1,000 chars with Read more, meta image preferred, events created without a failed image |
| V8 | Self-links (hash routes collapse to the feed url) are not pages; one read per url per check; the `known` count. Dazzle yielded 134 events |
| V9 | `LocationSpawner` (OSM, 200 km fence, 150 m room rule). Swallow Hill's rooms stayed home: "Tuft Theatre at Swallow Hill Music" fuzzy-matches the venue |
| V10 | General feeds (`GeneralEventFeed`, Westword). The listing page matched the Buell and created two shows. The article page lost six real events on location |
| V11–V18 | Leads (`LocationLead`, `EventLead`, `EventScan`), direct LM reads (`LocationSchema`, `EventSchema`), address-first OSM search with a website tiebreak, nameless locations at an address, cost mapped into events |
| V19–V20 | A feed schema needs a title that matches. `HtmlTrimmer` keeps `<br>`, whose removal changed the sibling order selectors were written against |
| V21 | A fresh rescan of every feed. The JSON-LD selector never matched and `<time datetime>` with a date alone lost Squarespace times (Lions Lair to 0): both fixed |
| V22 | JSON-LD in place: 166 ticket links and 148 costs where there were none, Red Rocks from 0 to 28. Units (`#100`, `Ste 1400`) left out of OSM searches, and a location lead created at its address when the map knows another name or none: 9 new Aurora venues. But the JSON-LD pass skipped the LM and lost descriptions where the page declared none (Larimer, Lost Lake) |
| V23 | JSON-LD supplements the LM at resolution time: descriptions back at Larimer, Lost Lake and Globe Hall (0 to 376, 495 and 820 chars on average), Fillmore 0 to 26 events. Parallel leads, the LM told when its html was cut, and only a `Basic` fetch asked whether scripting is required |
| V24 | A location's relative events and image urls resolved against its page (Ksoup's `absUrl` takes an attribute name, so they were stored raw). A unit of a single letter (`Suite D`) left out of map searches. Scripted fetches load stylesheets: Square Online hides its text until they load. Launch Pad, Muse Noraebang and Roaming Gnome created |
| V25 | `HtmlTrimmer` keeps `form`: ASP.NET pages wrap the whole page in one, and auroragov.org trimmed from 46,577 text chars to 95, which the LM then took for a page that needs scripting. Read in full after, but six of them failed to create on an image url holding spaces, and a UTF-8 byte order mark made Bally's "Not html" |
| V26 | Image urls with spaces encoded, crawler-created locations kept when their image fails, `looksLikeHtml` allowing a byte order mark, and `building`/`bldg` units left out of map searches. A lead may carry its page's html, sent by the browser extension, read in place of the first fetch. Calliope created from provided content; Facebook and Swallow Hill events read the same way. The six auroragov pages were restaged too |
| V27 | Locations created at an address take only the address from the map, never the type, hours, rank or website of the business it names there (26 old rows cleared). The road's word in an address search is taken before the first comma, so a street line holding its city (Meetup) still finds its house. Meetup still unlocated: its street line held the city, state and zip, and the area added again made OSM return nothing |
| V28 | An address is searched by its street before the first comma, with the area added once. Meetup's event landed at an unnamed location on North York Street. The Meetup Denver feed was added and read: 8 of 30 events created, 20 lost for placing by name alone, then an OSM `opening_hours` rule with a second day range after a comma threw and ended the check |
| V29 | Events of a general feed that give an address are placed as event leads are (`LocationSpawner.place`), fenced to 200 km of the feed. JSON-LD takes the first non-virtual `location`, and its area travels as `Area`. `osmHoursToSchedule` skips a rule it cannot read rather than throwing. Meetup Denver: 14 created and 5 duplicates of 30, 12 locations spawned. The rest: suburbs given as `town`, not `city` (3), a wrong street suffix on Meetup's own page, and parks and lots below OSM rank 30 |
| V30 | `minPlaceRank` gone: a map result is taken unless it is an area (`place`), a road (`highway`) or an administrative boundary, so parks count. A map address's town or village is its city. Place names are cleaned of bracketed notes and "parking lot" before searching. Meetup Denver: 3 created and 19 duplicates of 30 (its events were not cleared), 8 unlocated: 3 name no place a person goes, 2 lie at a house the map lacks, and 3 are the lessons below (a suburb labeled Denver, a road named by its route, a wrong suffix) |
| V31 | A JSON-LD image that is not absolute is dropped, not resolved: Meetup's is its stock fallback cover (`/images/fallbacks/…`), and raw it reached `provisionImageAndStore`, whose `error()` ended the Castle Rock check. The V30 run of 11 new Meetup city feeds made 70 events and 63 locations before 8 checks were cut short. V31 made 100 events and 79 locations, then Colorado Springs died on the page's meta image, relative too |
| V32 | A meta image that is not absolute is dropped too. Follow-up map searches: a place's name with its state alone (`Region`), then its name alone when the feed's point fences it, and an address with its state alone, since Meetup labels suburbs Denver and misnames streets ("17th St." for 17th Avenue). Of several hits, one on the address's road wins. By hand, these place 8 of V31's 60 unlocated. Run: 81 created, no failed checks, 13 of 56 reread strays placed; 12 places found but not created, parks and trailheads the map gives no city (Castlewood Canyon, Lair o' the Bear, Inlet Bay Marina). **The Blue Bonnet problem**: a name alone can find the wrong place ("Blue Bonnet" found a building on Dorsey Drive), and no data is preferred over bad data |
| V33 | A location needs only a point: no city is required to create or edit one, and a city named but not found still fails. Cityless locations stay off city feeds and city search. The slug base is the name, else the address, with its city when it has one, else the id. Stopped mid-run |
| V34 | A location lead may carry a stored location, whose gaps it fills from the page (`mergeAndUpdateLocation`), every stored value kept and hosted locations untouched. Each location with a website and no `checked_at` is read this way once, after the feeds and stored leads. Stopped mid-run to pace OSM at 2 s |
| V35 | `lead.is_rsvp` (set on the Meetup feeds): their events carry an `RSVP` link to the event page and a note in the description that an RSVP may be required. OSM searches paced at 2 s. JSON-LD text has its html entities decoded (WordPress writes `&#038;` for `&`); 73 stored events cleaned in SQL (dump `logs/schema/event-entities-before-decode.json`). Images: `imageUrl` reads `src`, else `data-src` or `data-lazy-src` (a `data:` placeholder sank Grimm Brothers' check), and an event or location keeps only an absolute http image. `Lead.isExternalOrigin`: a relative meta or JSON-LD image is resolved against the page for a location's feed or lead, and dropped for a general feed or event lead. Eventbrite Denver added. Run: Eventbrite 16 created of 20, a cross-source duplicate caught (Milagro's Denver Margarita Festival at Number 38) |
| V36 | Crawled events and page-read location descriptions end with an italic note that the information was gathered automatically, linking the source; RSVP events carry the RSVP note instead. Not yet read |

## Open Leads

Found and not yet acted on, each to be weighed against the General Model:

* **Declared events on feeds.** Feed pages declare their events in JSON-LD: Eventbrite as one `ItemList` (20, `startDate` date only), Meetup as bare arrays across 7 blocks (51), Luma as one `ItemList` (10), AllEvents as an array (77) and an `ItemList` (19). Each declared event carries its page `url`. `pageLdEvent` keeps only a page's own event, so a feed gets none of them. The path: in `crawlEventFeed`, match each record to the declared event of the same normalized `url` and lay it over `rawFeedEvent`, which `EventPage` carries on and the early `deliverEvent` paths use. A date-only declared date never overrules a read with a time. Perhaps enough to create an event without its page.
* **Luma.** Its Basic cards hold title and venue but no date, and their link is an empty overlay anchor (`a.event-link`, `&nbsp;`) the LM leaves null, so the declared events cannot be matched by url. Its scripted render puts the date in a heading above each day's cards, outside the event element, and shows fewer events (5 against 16). The LM answered `isScriptingRequired: false`.

* **Mark a lead checked when its check finishes.** Keep the ids of leads in flight in one in-memory set, left out of the checkable queries with `notInList`, and set `checked_at` after the check returns. A stopped run would then need no restaging. Tabled 2026-10-01.

* **Eventbrite's rate limit.** A probe drew `429` after about 37 page requests 1.2 s apart. Three in a row strike the origin and bench it for the check. The first run of 61 feeds drew them; the default delay went to 2 s and the workers to 16, then to 8 s when 429s came after about 20 s. Eventbrite answers 429 with no `Retry-After` even to a lone request well after a run.

* **Roads named by their route.** "22550 CO-74" finds the house at 22550 Bear Creek Road (Lair o' the Bear), but `findAddress` wants the road to hold "CO-74" and drops it.
* **Wrong street suffixes.** "1298 South Broadway Avenue" finds nothing; "1298 South Broadway" finds Maria Empanada. Seen twice, both on Meetup's own pages.

* **A place sharing an element with the date.** Newspaper listings put date, time and place in one `<strong>` split by `<br>`. The whole line goes to OSM and finds nothing.
* **Date parts sharing a class.** Summit and Marquis hold the date in each card as `<time><p>Wed</p><p>30</p><p>Sep</p></time>`, and the weekday and month share one class, so the LM's selectors find the weekday and the date is dropped (V23). The whole `<time>` element's text would parse.
* **A date selector on the title.** Bar 404's date selector reads the event title, and validation let it through.
* **Load more.** Swallow Hill's feed shows 10 events behind a "load more" button; only the first page is read.
* **Feeds of feeds.** Westword's listing links to articles that are feeds themselves. A general lead classifier was ruled out; the crawler reads kinds it knows up front.
* **Featured content on event pages.** A sidebar of other events (a run of same-class cards) can win the description selector, as on the Aurora library's pages. A trim anchored on the declared title and description, dropping repeated card runs, is on the map but waits for a problem JSON-LD does not already solve.
* **Meta image over the LM's image.** The page's meta image still outranks the LM's image, the one place meta outranks a read. Undecided.
* **Locations created at an address** carry the page's name but no map id. They want `needsReview` once the review rigging is reworked.
* **Performers** are read into `LdEvent` and wait for the model to hold them.
* **Concurrency edges.** The same url read by two leads at once is fetched twice (spaced by its gate); the OSM `places` cache never expires; `server.createLocation` may look up a new city through the server's own OSM client, outside the crawler's gate.

## Accepted as Unread

Roxy (an iframe widget that arrives late), Black Box and Squire (calendar grids), and hash-route pages. They wait until their need is seen across many origins.

* **hi-dive** sits behind an automatic "verifying your request" page; a bot filter is respected, not waited out.
* **Whispers on Havana**'s homepage holds no text beyond its name.
* **Date runs** such as Denver Center's "Sep 11 – Oct 4": the event model holds one date.
* **Westword**: its lists hold bare text with no links, and it is not a planned source.
