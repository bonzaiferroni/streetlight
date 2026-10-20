// The earth shot: the map zooms in on a marker, then the pointer clicks it and the focus panel scrolls.
// It reads the dev server's data, so its marker must still be on the map.
import { chromium } from 'playwright';
import { arg, bezier, easeInOut, encode, encodeOnly, fpsOf, pointerCss, pointerSvg, progress, quality, segmentDirs } from './promo.mjs';

// the map's tiles and markers flicker and pop in real time, so this shot freezes the page clock and steps it itself:
// each frame sets performance.now, seeks every animation, and swaps the animated icons for copies begun at its time

// --ui-scale=1.2 renders as a browser zoomed to 120%, keeping the output size
const uiScale = Number(arg('ui-scale') ?? 1);
const hiddenPointer = { x: -100, y: -100, scale: 1, opacity: 0 };

/**
 * The pointer at [s] seconds into a tap: it arcs from [from] past [bend] to [target], presses, then fades.
 * Coordinates of [from] and [bend] are in output pixels; [target] is in page pixels.
 */
const tapPointer = (s, { from, bend, target, travelSeconds, pressSeconds, hideSeconds }) => {
    const [x, y] = bezier(from.map(v => v / uiScale), bend.map(v => v / uiScale), target, easeInOut(progress(s, 0, travelSeconds)));
    const press = Math.sin(Math.PI * progress(s, travelSeconds, pressSeconds));
    return { x, y, scale: 1 - 0.15 * press, opacity: 1 - progress(s, travelSeconds + pressSeconds, hideSeconds) };
};

/** Launches a browser and a page sized for [shot], with performance.now ready to freeze. */
const launch = async shot => {
    const browser = await chromium.launch({ channel: 'chromium', args: ['--ignore-gpu-blocklist', '--enable-gpu', '--use-angle=gl-egl'] });
    // Chrome snaps the scrolled layer to whole pixels at a fractional device scale, so the scale is rounded up and the encode downscales
    const viewport = { width: Math.round(shot.size.width / uiScale), height: Math.round(shot.size.height / uiScale) };
    const page = await browser.newPage({ viewport, deviceScaleFactor: Math.ceil(quality.scale * uiScale) });
    // performance.now runs on real time until frozen, then only moves when a frame is stepped
    await page.addInitScript(() => {
        const realNow = performance.now.bind(performance);
        let frozenAt = null;
        performance.now = () => frozenAt ?? realNow();
        globalThis.promoFreeze = () => frozenAt = realNow();
        globalThis.promoSetNow = ms => frozenAt = ms;
    });
    return { browser, page };
};

/**
 * Adds [css] and the pointer to the page, freezes the clock, and installs `promoFrame`.
 *
 * Before rigging, a shot may define `promoApply(state)` in the page to set its frame state, and `promoRender()` to
 * resolve once a frame is fully drawn. Without one, a frame is drawn after two animation frames.
 */
const rig = async (page, css = '') => {
    await page.addStyleTag({ content: pointerCss + css });
    await page.evaluate(pointerSvg => {
        document.body.insertAdjacentHTML('beforeend', pointerSvg);
        globalThis.promoClock = { origin: new Map(), base: promoFreeze(), started: false, svgs: {} };
        globalThis.promoRender ??= () => new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r)));
        // animated icons are SMIL in a mask image, which runs on its own real-time clock; each frame swaps in a copy begun at our time
        globalThis.promoIcons = ms => Promise.all([...document.querySelectorAll('.magic-icon')].map(async el => {
            el.dataset.promoSrc ??= getComputedStyle(el).getPropertyValue('--mask-url-animated').match(/url\(["']?(.*?)["']?\)/)?.[1] ?? '';
            const src = el.dataset.promoSrc;
            if (!src) return;
            promoClock.svgs[src] ??= fetch(src).then(r => r.text());
            const svg = (await promoClock.svgs[src]).replace(/<(animate\w*|set)\b([^>]*?)(\/?)>/g,
                (_, tag, attrs, close) => `<${tag}${attrs.replace(/\sbegin="[^"]*"/, '')} begin="-${ms / 1000}s"${close}>`);
            const url = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
            const image = new Image();
            image.src = url;
            await image.decode().catch(() => {});
            el.style.maskImage = `url("${url}")`;
        }));
        // animations running at the freeze keep their phase; later ones start from the frame they first appear in
        globalThis.promoSeek = ms => {
            for (const a of document.getAnimations()) {
                if (!promoClock.origin.has(a)) promoClock.origin.set(a, (promoClock.started ? 0 : a.currentTime ?? 0) - ms);
                a.pause();
                a.currentTime = promoClock.origin.get(a) + ms;
            }
            promoClock.started = true;
        };
        globalThis.promoFrame = async state => {
            const { ms, pointer } = state;
            promoSetNow(promoClock.base + ms);
            await globalThis.promoApply?.(state);
            const el = document.getElementById('promo-pointer');
            el.style.opacity = pointer.opacity;
            el.style.transform = `translate(${pointer.x}px, ${pointer.y}px) scale(${pointer.scale})`;
            promoSeek(ms);
            await promoRender();
            await promoIcons(ms);
            // animations that arrived while waiting started in real time; seek them before the capture
            promoSeek(ms);
            await promoRender();
        };
    }, pointerSvg);
};

/** A function that steps the page to a frame state and saves it as the next numbered frame in [dir]. */
const frameWriter = (page, dir) => {
    let index = 0;
    return async state => {
        await page.evaluate(state => promoFrame(state), state);
        await page.screenshot({ path: `${dir}/${String(index++).padStart(4, '0')}.png`, animations: 'allow' });
    };
};


const shot = {
    name: 'fizzbuzz',
    url: 'http://localhost:8080/earth',
    size: { width: 540, height: 480 },
    fps: fpsOf(30),
    center: [-104.9941978, 39.7466116],
    marker: 'The Binary Blues',
    zoom: { from: 14, to: 16.5, seconds: 3, restSeconds: 0.5 },
    focus: {
        pointerFrom: [690, 540],
        pointerBend: [600, 330],
        travelSeconds: 1.2,
        pressSeconds: 0.15,
        hideSeconds: 0.3,
        scrollTo: 720,
        scrollStart: 2,
        scrollSeconds: 6,
        holdSeconds: 1,
    },
    loopFadeSeconds: 0.75,
};

const hideUi = `
    #app-viewport, #app-overlay, #toaster,
    #earth > :not(.map-mount) { visibility: hidden; }
    .earth-header { display: none; }
    .earth-focus { visibility: visible; scrollbar-width: none; }
    /* a translation alone is snapped to whole pixels; a negligible rotation keeps the scrolled content at fractional offsets */
    .earth-focus > * { will-change: transform; rotate: 0.0001deg; }
    .maplibregl-control-container { display: none; }
`;

const { fps } = shot;
const z = shot.zoom;
const f = shot.focus;
const zoomEnd = z.seconds + z.restSeconds;
const focusSeconds = f.scrollStart + f.scrollSeconds + f.holdSeconds + shot.loopFadeSeconds;
const dirs = await segmentDirs(shot, 'zoom', 'focus');

if (!encodeOnly) {
    const { browser, page } = await launch(shot);
    await page.goto(shot.url);
    await page.waitForFunction(() => globalThis.geoMapWindow?.widget?.loaded());

    // settle the opening camera, markers and their images before the clock starts
    await page.evaluate(({ center, zoom }) => {
        geoMapWindow.widget.jumpTo({ center, zoom });
    }, { center: shot.center, zoom: z.from });
    await page.waitForLoadState('networkidle');
    await page.waitForFunction(() => [...document.querySelectorAll('.maplibregl-marker img')].every(i => i.complete));

    await page.evaluate(() => {
        const map = geoMapWindow.widget;
        const rendered = () => new Promise(resolve => { map.once('render', resolve); map.triggerRepaint(); });
        globalThis.promoApply = ({ zoom, scroll }) => {
            if (zoom != null) map.jumpTo({ zoom });
            // scrollTop snaps to whole pixels; translating the content moves it by fractions
            if (scroll != null) document.querySelector('.earth-focus > *').style.translate = `0 ${-scroll}px`;
        };
        // with time frozen, fades never finish and 'idle' never fires, so wait for a render with every tile in
        globalThis.promoRender = async () => {
            do await rendered(); while (!map.areTilesLoaded());
        };
    });
    await rig(page, hideUi);

    // segment 1: the zoom, rendered as `sub` samples per frame to be blended into motion blur
    const captureZoom = frameWriter(page, dirs.zoom);
    const zoomSamples = zoomEnd * fps * quality.sub;
    for (let i = 0; i < zoomSamples; i++) {
        const ms = i * 1000 / (fps * quality.sub);
        const t = Math.min(ms / (z.seconds * 1000 - 1000 / fps), 1);
        await captureZoom({ ms, zoom: z.from + (z.to - z.from) * easeInOut(t), pointer: hiddenPointer });
    }

    // segment 2: the pointer clicks the marker, then the focus panel scrolls
    const captureFocus = frameWriter(page, dirs.focus);
    const thumb = await page.locator('.maplibregl-marker', { hasText: shot.marker }).locator('.marker-thumb').boundingBox();
    const target = [thumb.x + thumb.width / 2, thumb.y + thumb.height / 2];
    const tap = { from: f.pointerFrom, bend: f.pointerBend, target, ...f };
    const clickAt = f.travelSeconds + f.pressSeconds / 2;
    let clicked = false;
    for (let i = 0; i < focusSeconds * fps; i++) {
        const s = i / fps;
        const pointer = tapPointer(s, tap);
        if (s < f.travelSeconds) await page.mouse.move(pointer.x, pointer.y);
        if (!clicked && s >= clickAt) {
            clicked = true;
            await page.mouse.click(...target);
            await page.waitForSelector('.earth-focus .feed-row');
            await page.waitForLoadState('networkidle');
            // load offscreen images now, so none pops in mid-scroll
            await page.evaluate(() => document.querySelectorAll('.earth-focus img').forEach(i => i.loading = 'eager'));
            await page.waitForFunction(() => [...document.querySelectorAll('.earth-focus img')].every(i => i.complete));
        }
        const scroll = clicked ? f.scrollTo * easeInOut(progress(s, f.scrollStart, f.scrollSeconds)) : null;
        await captureFocus({ ms: (zoomEnd + s) * 1000, scroll, pointer });
    }
    await browser.close();
}

// blend the zoom samples, join the focus, and fade the end back into the first frame for a seamless loop
const { width, height } = shot.size;
const { sub } = quality;
await encode({
    shot,
    fps,
    inputs: [{ dir: dirs.zoom, rate: fps * sub }, { dir: dirs.focus, rate: fps }],
    filter: [
        `[0]tmix=frames=${sub},select='eq(mod(n\\,${sub})\\,${sub - 1})',settb=1/${fps},setpts=N,fps=${fps},scale=${width}:${height}:flags=lanczos,split[zoom][first]`,
        `[1]scale=${width}:${height}:flags=lanczos,settb=1/${fps},setpts=N,fps=${fps}[focus]`,
        `[first]trim=end_frame=1,loop=loop=${Math.ceil(shot.loopFadeSeconds * fps)}:size=1,settb=1/${fps},setpts=N,fps=${fps}[still]`,
        `[zoom][focus]concat=n=2,settb=1/${fps},setpts=N,fps=${fps}[body]`,
        `[body][still]xfade=transition=fade:duration=${shot.loopFadeSeconds}:offset=${zoomEnd + focusSeconds - shot.loopFadeSeconds}`,
    ].join(';'),
    seconds: zoomEnd + focusSeconds,
});
