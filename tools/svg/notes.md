# Icon Work — Record

The specification is `docs/packages/tools.svg.md`. This file records the work: what each script built, what each new icon replaced, and what was rejected and why.

Task: an `label-animated.svg` for every pack in `koala/.../SvgFile.kt` (`svgPackOf` entries), now extended to new packs for values whose icon stood in for something else.

## Progress
- [x] Mask test across engines: SMIL animates inside CSS mask-image in chromium, firefox, webkit
- [x] Pilot batch written (tools/batches/pilot.py)
- [x] Navigator review of pilot (clock sped to 2s/24s, atom electrons offset)
- [x] Brands: pulse 6%/3s (tools/batches/brands.py)
- [x] Sweep: 8 forks, lists in tools/svg/lists/NN.txt, scripts tools/batches/sweep-NN.py, notes tools/svg/progress/NN.md
- Milestone voice: tools/announce.sh (background) speaks each 10%
- [x] Full sweep: 541/541 packs animated, all parse, all SMIL, all loop
- Post-fix (not in sweep scripts; rerunning a sweep script reverts it): the 10 files with `stroke-dasharray="1 1"` + pathLength=1 got gap 1.1 and dashoffset values x1.05, so a fully hidden line leaves no round-cap dot (circle-check, calendar-check, check, heart-off, heart-broken, heartbeat, signature, shield-check, star-off, table).
- Expected FRAME0 diffs (added features): vehicle hubs, building-castle pennant, vinyl groove, video dot, traffic-lights, movie (clip edge, 42px). rays/social: their -large files stroke white, animated use currentColor.
- Swing smoothing: `tools/smooth.py` re-splines keys that pass through rest between opposite extremes (and at the loop seam) so swings don't pause mid-way. Applied to 113 files. Rerun it after any regeneration.
- Preview is grouped by enum (EventTag, LocationTag, *Svg), then packs outside enums; animated only.
- Forged icons (our own, Tabler style): tools/forge.py builds static small/large/very-large + animated for drum, violin, trumpet, saxophone, flute, acoustic-guitar, boxing-glove, disco-ball, juggling, hot-spring. fountain is Tabler's (tools/batches/fountain.py). Enum values repointed to them.
- Forged round 2: dance, playground, tram, accordion, harp, electric-guitar, synthesizer, barber-pole, ferris-wheel, toy-blocks (forge.py). Round-1 rework: juggling hands catch/toss, disco meridians sweep one way, hot-spring steam coffee-style, fountain plumes spurt (dasharray).
- Forged round 3: fireworks, family (forge.py); EventTag.Holiday repointed from gift, KidsAndFamily from mood-kid.
- flame pack (tools/batches/flame.py) from www/svg/flame.svg, keeps its own tight viewBox; top and right tips rise out of phase. SvgFile.Flame is now the pack.
- Legs as single arcs: dance (forge.py), yoga (tools/batches/yoga-legs.py edits the Tabler files in place).
- yoga animated is now built by tools/batches/yoga-warrior.py (arms rise, body sinks half as far, feet planted); run it after yoga-legs.py.
- building-store animated (tools/batches/building-store.py): awning flaps skew in turn right to left like a breeze; replaces the sweep pulse.
- Review round 1 (tools/batches/review-1.py): presentation, needle-thread (thread draws from left end, tail follows), tools-kitchen (fork then knife lift once/5s), rainbow (quick wave then rest), building-church (cross swells, door opens). Preview has an "Under review" section showing icons as flair (mask, event color, opacity .25, 20rem).
- KEEP dance as is (navigator wants it, used by Dancer, DanceInstructor, DanceStudio, Swing). If forge.py ever reruns it, rerun smooth.py on dance-animated.svg after.
- mood-crazy-happy: mouth hand-edited in all 5 files to an open laugh (straight top, curved bottom); filled uses evenodd with the mouth as a hole. A sweep rerun would revert it.
- Hand edits: movie animated (side ticks every 4 travel with one long divider every 16, so one shows at a time; 16 units/8s), mood-crazy-happy eyes 4s, ball-volleyball 16s.
- trekking reworked (tools/batches/trekking.py, then smooth.py): hat, arcs, only the right arm and stick swing.
- Packs moved to www/icon (2026-10-10); tools repointed. Old notes saying www/svg mean www/icon.

## Stick Figures (2026-10-19)
Set by yoga (Wellness), which the navigator approved; its rules are in the spec under Figure Style.
- yoga: scale 0.9, down 1.75, head r 2.5 before scaling.
- The head never flickers: a burning head reads as alarming.
- yoga: tools/batches/yoga-flame.py; supersedes yoga-legs.py and yoga-warrior.py (rerunning yoga-warrior.py reverts the head).
- Head size: the flame reads only from about r 2 on the icon (r 1.8 at the least); below that it closes up at every stroke width. Tabler's r 1 and 1.5 heads grow to r 2 to 2.2 (before scaling) and the figure shrinks to about 0.92.
- The others: tools/batches/stick-figures.py [labels] (helpers in tools/stick.py): walk, run, karate, bike, ski-jumping, old, pray, stretching, swimming, massage, trekking, dance. It supersedes the sweep, trekking.py and forge.py for these; rerunning those reverts the heads. smooth.py is not needed for them.
- Each keeps its old motion, now carrying the head with the body: pray bows its upper body about the hip, stretching leans its upper body, bike's rider bobs together with the head.
- trekking lost its hat; the flame takes the head's place.
- massage: both people have flame heads; the client's sits upright at the end of the table.
- dance: the head sits left of the old one so the raised arm clears it through the sway.
- juggling and the busts (user, users-group, family, friends) are not stick figures and keep their heads.

## Rigged Figures (2026-10-19)
- tools/batches/rigged.py [labels]: bike, ski-jumping, pray, stretching, massage (walk, run, old, swimming there are superseded by gait.py; rerunning rigged.py without labels reverts them). Supersedes stick-figures.py for these. Pose 0 is the static icon.
- Motion is two poses eased (a swing comes to rest at its extremes), except bike's pedal: 8 IK keyframes, linear, foot on the crank circle.
- The head rides its anchor joint with a translate, so it stays upright and static.
- `bob` lifts the whole figure twice per loop (walk, run: highest as the legs pass).
- Motions: walk and run swing legs and counter-swing arms; bike pedals, hand pinned to the bars; ski-jumping floats, the V opens 3° about the feet and the arms lift; old steps with the cane rocking on its tip, hand pinned to the grip; pray bows from the hip and neck; stretching sinks into the lunge, feet pinned, upper body reaching; swimming's arm recovers forward as the body dips; massage leans in, hands pressing.
- Size: 1.8 to 3.8 KB per animated file (was 0.6 to 1.1); rig numbers are written at two decimals.
- old: the cane moved under the hand (shaft at x 14), so the hand grips it.
- bike: one leg, as Tabler draws it; a second leg tangled at icon size.

## Gait Figures (2026-10-19)
- tools/batches/gait.py [labels]: walk, run, old, swimming. Full cycles: legs and arms alternate through the whole stride. Supersedes rigged.py for these.
- One cycle sampled into 12 keyframes, played linearly; frame 0 is the static icon, tuned to Tabler's pose (wide stride, back arm near horizontal).
- Feet follow `foot_path`: planted and sliding back through the stance fraction, then lifted and carried forward (`early` < 1 lifts sooner, a run's heel kick); knees by IK, always forward. Arms swing against the legs by `arm(shoulder, swing, bend)`.
- Depth: near leg and arm join the body; the far leg starts 45% down the thigh and the far arm 45% down the upper arm. The near/far identity never swaps, so the gap always marks the far side.
- `bob_at` lifts the hips twice per cycle, highest at mid-stance (walk, old) or in flight (run).
- run: driven by joint angles, not a foot path. `RUN_HIP` and `RUN_KNEE` in gait.py are periodic curves over the stride (landing at 0, toe-off at 0.35), shaped on running gait data: hip about 75° of arc peaking in late swing, knee about 40° in stance and near 120° in swing (the heel kick). The hips stand on whichever foot is planted and fly between steps (`flight`), so the bounce comes from the legs. The whole body tilts `lean` (20°) as one line; the hip angles turn into the world by 0.6 of it. 16 keyframes over 0.8s; frame 0 is the near push-off. Arms by `pump`, opposite the legs, swung from the leaning trunk (not vertical): about 55° behind it and 30° ahead, the elbow near 90°, tighter forward (hand to the chest) and opening behind. 8.8 KB.
- old: the cane's tip follows its own foot path in step with the far leg, the cane tilting with the stride; the hand is pinned to the grip by IK.
- swimming: both arms turn full circles half a turn apart; the forearm hangs back above water and straightens below. A clipPath at the wave line hides the stroke under water, and a mask of the head (stroke 4, moving with it) hides the arms passing behind the head. Statics carry the same clip and mask so frame 0 matches.
- walk: the hips stand on the planted leg (`stand`), near straight and flexed 2.5% at mid-stance, so they vault over it rather than bob by a fixed curve. Stance 0.5 with the stride centred on the hips, so frame 0 has both legs straight in Tabler's wide stride; the knee bends only in swing.
- trekking: walk's foot path at a heavier pace (1.6s, bob 0.35), leaning on the pack. The pole's tip follows its own foot path in step with the far leg, tilting ±10° with the stride; the near hand is pinned to it 7 up the shaft by IK. Pack (with a top flap seam) and pole are morphed props riding the bob. Legs sum 7.65, short of the hip-to-backstride reach, so the back leg straightens. Supersedes stick-figures.py for trekking.
- Size: 5.6 to 8.5 KB per animated file.

## New Icons (2026-10-19)
Values whose icon stood in for something else get their own, forged in the Tabler style, registered in SvgFile and repointed in their enum.

Round 1, InstrumentSvg (tools/batches/instruments.py [labels], forge.py helpers):
- bagpipes (was wave-sine): tilted bag, three drones fanned over the shoulder, blowpipe, chanter. The bag squeezes and refills; drones sway about their bases, alternating directions.
- harmonica (wave-square): case, cover plates, holes. Slides side to side.
- tambourine (crosshairs): hoop with five jingles. A shake, the jingles rattling, then rest.
- cymbals (disc): crash with bell on a tripod stand. Struck, it wobbles and settles.
- cajon (box): box drum with corner screws on the playing face and a port on the side. Boom then tak: the box flexes from its base.
- xylophone (piano): four bars falling in length, two mallets leaning outward. They play `PHRASE` (bars 0 3 1 2 0 2 1 3, hands alternating, left on bars 0 and 1) over 3.2s: each mallet hovers over its next bar, drops onto it on its beat, and the struck bar dips. Keys past 1 are invalid SMIL, so a late beat's dip must end by the loop's end.
- trombone (trumpet): bell over a long slide with a back crook. The slide reaches out 3 and back; the inner tubes run to x 16 so the outer slide always overlaps them.
- tuba (trumpet): bell open upward over a coiled body, two valves, mouthpiece. Rocks on the beat as the valves press.
- banjo (acoustic-guitar): round pot on a long neck with the fifth peg, upright and shown at 40°. A bright pluck, twice.
- hand-drum (drum): a djembe, goblet body, rope lacing, head. The head gives under two strikes.
- Small-size rule learned: details closer than about 2.5 units merge at stroke 2 (banjo's inner hoop, xylophone rails, tuba's third valve were dropped for it).
- Still open in InstrumentSvg: Ukulele, Mandolin, Sitar (acoustic-guitar); Viola, Cello, DoubleBass (violin); FrenchHorn (trumpet); Organ, Keyboard (synthesizer); Clarinet, Oboe (flute); Percussion (drum); Beatbox (wave-sine).

Round 2, across enums (tools/batches/round2.py [labels]; figures via `figure_frames`, flame head marked `data-head`, half-weight strokes marked `data-thin`):
- party-hat (EventTag.Party, not yet repointed: the value is labeled "Watch Party", so the TV fits until it becomes Party): striped cone, pompom, confetti. Hops and wiggles; confetti spins.
- billiards (LocationTag.Billiards, was ball-bowling): cue aimed along a diagonal, cue ball, eight ball. Two practice strokes, strike, the cue ball kisses the eight, which rolls on; the balls fade out, return and fade in.
- trampoline (TrampolinePark, was jump-rope): frontal jumper at the apex, arms out. Falls (gravity spline), sinks the mat into a squat, springs back up.
- marionette (TalentSvg.Puppeteer, was ghost): cross control, three half-weight strings to the raised hands and the head, a compact puppet hanging low. The control rocks; hands follow and the leg under the rising hand kicks.
- tightrope (TalentSvg.Acrobat, was stretching): rope between posts, frontal walker with a long pole. The body sways about the feet; the pole tilts against it.
- taxi (VehicleSvg.Taxi, was car): Tabler's car with a roof sign; car's motion.
- subway (VehicleSvg.Subway, was locomotive): train front in a tunnel arch over rails. Sways; headlights glow.
- pier (LocationTag.Pier, was sailboat): deck slab on three pilings, lamp post at the end, two rows of waves. The rows drift opposite ways, a wavelength per loop, clipped to the water.
- drive-in (LocationTag.DriveIn, was car): wide screen on legs, a car from behind. On screen the sun sets behind the hills (clipped to the sky).
- shower (ResourceSvg.Shower, was bath): arm, head, three dashed streams. Water falls by stroke-dashoffset.
- rig.py `morph`/`slide` keyTimes now at three decimals (two gave 0.12 for 0.125).

Round 3, across enums (tools/batches/round3.py [labels], reusing round2's packer):
- clown (TalentTag.Clown, was mood-tongue): round face, ball nose, wide smile, hair tufts. The nose honks twice, the tufts puff outward from their roots (a rotation swung their ends into the face), the smile widens.
- top-hat (TalentTag.Magician, was wand, shared with Conductor): hat with a straight brim, wand, two sparkles. The wand pivots at its handle so its tip taps the crown twice, sparkles twinkle, rabbit ears peek out of the hat (clipped above the crown).
- pen-nib (TalentTag.Calligrapher, was signature, shared with Caricaturist): a nib at the end of its swash. The swash fades, the nib glides back along it (animateMotion) and writes it again (dashoffset).
- beatbox (TalentTag.Beatboxer, InstrumentSvg.Beatbox, was wave-sine): bust in profile (head on shoulders), two arcs from the mouth. The head nods about the neck; inner and outer arcs flash out of phase, boom and tss.
- french-horn (FrenchHorn, was trumpet): coil, inner loop, lead pipe, a bell leaving the coil tangentially. Rocks and swells on two notes.
- clarinet (Clarinet and Oboe, were flute): tube at 30° with beak mouthpiece, barrel ring, flared bell. Holes close in turn as it tilts.
- organ (Organ, was synthesizer): five pipes with mouths over a console. Each pipe rises as its note sounds.
- water-slide (LocationTag.WaterPark, was swimming): ladder tower, winding slide, pool. A rider slides down and splashes; the pool drifts.
- speakeasy (LocationTag.Speakeasy, was key): door with a peephole slot. The eyes glance, the slot shuts and opens.
- race-flag (LocationTag.Racetrack, was steering-wheel, kept for GoKartTrack): checkered flag on a pole, filled squares on a waving grid (12 frames, linear).

Round 4, TalentTag (tools/batches/round4.py [labels]):
- lectern (Speaker, was presentation): a bust behind Tabler's podium with its gooseneck mic. Leans in, nods as they talk.
- model-pose (Model, was camera-selfie): bone rig (rig.py Rig + round2 figure_frames), feet pinned and hands placed by IK. Two mirrored stances, held and eased over 3.2s: left hand on hip with weight left (static), then right hand on hip with weight right. A sparkle flash pops as each lands. (A three-pose version with raised arms read as too elaborate.)
- paint-roller: scrapped (bricks, a painted figure and a painted landscape all read poorly). Muralist uses palette; the files and SvgFile.PaintRoller are gone.
- caricature (Caricaturist, was signature): easel with a flame-headed bust (head and shoulders) on its pad. The whole top page, board outline and drawing, flips up over the easel and fades; the pencil draws the head then the shoulders on the fresh page. Its tip rides the ink: one animateMotion over head+shoulders with calcMode linear (the default, paced, ignores keyPoints), keyPoints split by path length. The base board and ink hide while the top page covers them, so frame 0 draws each line once. Swaps happen only under an opaque top page: it returns over the finished drawing (0.88), then the ink and base board hide (0.9); swapping both at once made the figure blink.
- bouquet (Florist, was flower): three scalloped blooms over a paper wrap with a bow. Blooms sway in turn.
- Removed from TalentTag the same day: Band, Conductor, CircusPerformer, DragPerformer, Volunteer.

Composition, SkillTag (tools/batches/compositions.py):
- songwriting (Songwriter, was writing): Tabler's music, its right note head dropped and stem stopped at y 11, with
  Tabler's -edit pencil in the corner. The pencil scribbles, then the note bobs twice.
