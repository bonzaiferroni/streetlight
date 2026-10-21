// The layout showcase: FizzBuzz's page holds still in each header layout, the hero, the marquee, and the card, each
// blurring and fading into the next. Then the hero again from the foot of the page, scrolling up to its top, where
// the shot began, so it loops.
//
// The page's lights hold still, so a layout is filmed as one still and only the scroll as frames. The transitions
// are drawn between stills, since the page can only blur from one layout to the next.
import { assemble, buildShots, easeInOut, encode, encodeOnly, fpsOf, frameWriter, openStudio, segmentDirs, serveShot } from './promo.mjs';

const shot = {
    name: 'layout-showcase',
    size: { width: 960, height: 600 },
    // wider than the content panel, so the hero's backdrop reaches past the body
    viewport: { width: 1280, height: 800 },
    fps: fpsOf(30),
    still: true,
    // seconds each layout holds, each transition takes, the foot of the page holds, and the scroll to the top takes
    hold: 2,
    transition: 1,
    footHold: 0.5,
    scroll: 4.5,
    // the strongest blur of a transition, in shot pixels
    blur: 16,
};
shot.seconds = 3 * (shot.hold + shot.transition) + shot.footHold + shot.scroll;

// no pointer is drawn
const shotCss = `
    html { scrollbar-width: none; }
    #promo-pointer { display: none; }
`;

const { fps } = shot;
const dirs = await segmentDirs(shot, 'stills', 'scroll', 'page');
// in the order they are filmed; the hero is filmed in the same render as its scroll, so the two match where they meet
const stills = ['Marquee', 'Card', 'Hero', 'Foot'];
const still = name => `${dirs.stills}/${String(stills.indexOf(name)).padStart(4, '0')}.png`;
const scrollFrames = Math.round(shot.scroll * fps);

if (!encodeOnly) {
    await buildShots();
    const server = await serveShot(shot);
    const studio = await openStudio(shot);
    const { page } = studio;
    await studio.load(server.url);
    await studio.until(() => document.querySelector('.hero-header') && document.fonts.status === 'loaded');
    await studio.start(shotCss);

    const writeStill = frameWriter(dirs.stills, shot);
    for (const name of stills.slice(0, 3)) {
        await page.evaluate(name => promoHeader(name), name);
        await writeStill(await studio.settle());
    }
    const foot = await page.evaluate(() => document.scrollingElement.scrollHeight - innerHeight);
    await studio.scrollTo(foot);
    await writeStill(await studio.settle());

    const writeScroll = frameWriter(dirs.scroll, shot);
    for (let i = 1; i <= scrollFrames; i++) {
        await studio.scrollTo(Math.round(foot * (1 - easeInOut(i / scrollFrames))));
        await writeScroll(await studio.frame());
    }
    await studio.close();
    server.close();

    const { hold, transition: seconds, blur } = shot;
    await assemble(dirs.page, shot, [
        { still: still('Hero'), seconds: hold },
        { from: still('Hero'), to: still('Marquee'), seconds, blur },
        { still: still('Marquee'), seconds: hold },
        { from: still('Marquee'), to: still('Card'), seconds, blur },
        { still: still('Card'), seconds: hold },
        { from: still('Card'), to: still('Foot'), seconds, blur },
        { still: still('Foot'), seconds: shot.footHold },
        { frames: dirs.scroll, count: scrollFrames },
    ]);
}

await encode({ shot, fps, inputs: [{ dir: dirs.page, rate: fps }], seconds: shot.seconds });
