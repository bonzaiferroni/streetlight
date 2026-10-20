// Shared pipeline for promo shots. A shot's content is built from web/src/jsPromo and served locally, then filmed in
// a studio: Chrome's headless shell with begin frame control and virtual time, so the page runs exactly as in a
// browser while each frame lands on time. A camera crops each frame, and the frames are encoded to AV1.
import { chromium } from 'playwright';
import { mkdir, mkdtemp, readdir, readFile, rm, writeFile } from 'node:fs/promises';
import { spawnSync } from 'node:child_process';
import { createServer } from 'node:http';
import { homedir, tmpdir } from 'node:os';
import { extname, join, normalize, resolve } from 'node:path';
import { build } from 'esbuild';
import sharp from 'sharp';

// --name=value from the command line
export const arg = name => process.argv.find(a => a.startsWith(`--${name}=`))?.slice(name.length + 3);

// --encode-only reuses the frames of the last capture
export const encodeOnly = process.argv.includes('--encode-only');
// fast mode renders at the output size with a quick encode, for trying out composition
export const mode = process.argv.includes('--fast') ? 'fast' : 'full';
export const quality = {
    fast: { scale: 1, sub: 1, preset: 8 },
    full: { scale: 2, sub: 4, preset: 4 },
}[mode];
// --fps=60 overrides the shot's default
export const fpsOf = fallback => Number(arg('fps') ?? fallback);

export const easeInOut = t => t < 0.5 ? 4 * t * t * t : 1 - (-2 * t + 2) ** 3 / 2;
export const progress = (s, start, seconds) => Math.min(Math.max((s - start) / seconds, 0), 1);
export const bezier = (a, c, b, t) => a.map((_, k) => (1 - t) ** 2 * a[k] + 2 * (1 - t) * t * c[k] + t ** 2 * b[k]);
export const lerp = (a, b, t) => a.map((v, k) => v + (b[k] - v) * t);

export const pointerCss = `
    #promo-pointer {
        position: fixed;
        left: 0;
        top: 0;
        z-index: 2147483647;
        pointer-events: none;
        transform-origin: 0 0;
        filter: drop-shadow(0 1px 2px rgb(0 0 0 / 0.6));
    }
`;

export const pointerSvg = `<svg id="promo-pointer" width="22" height="30" viewBox="0 0 22 30">
    <path d="M1 1 L1 24 L7 18.5 L11 28 L15 26.3 L11 17 L19.5 17 Z" fill="white" stroke="black" stroke-width="1.5" stroke-linejoin="round"/>
</svg>`;

const root = resolve(import.meta.dirname, '../..');
const promoModule = `${root}/web/build/compileSync/js/promo/promoDevelopmentExecutable/kotlin/streetlight-web-promo.mjs`;
const bundleDir = `${import.meta.dirname}/bundle`;

/** Compiles the promo shots of `web/src/jsPromo` and bundles them into `bundle/`. `--no-build` skips both. */
export const buildShots = async () => {
    if (process.argv.includes('--no-build')) return;
    const gradle = spawnSync(`${root}/gradlew`, ['-q', ':web:jsPromoPromoDevelopmentExecutableCompileSync'], { cwd: root, stdio: 'inherit' });
    if (gradle.status !== 0) throw new Error('promo compile failed');
    await rm(bundleDir, { recursive: true, force: true });
    // the Kotlin output imports npm packages by bare name, which resolve from the root project's node_modules
    await build({
        entryPoints: { promo: promoModule },
        bundle: true,
        format: 'esm',
        splitting: true,
        outdir: bundleDir,
        logLevel: 'warning',
        nodePaths: [`${root}/build/js/node_modules`],
    });
};

const shotPage = `<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Fira+Sans:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap">
    <script type="module" src="/bundle/promo.js"></script>
</head>
<body></body>
</html>`;

const types = { '.js': 'text/javascript', '.css': 'text/css', '.svg': 'image/svg+xml', '.png': 'image/png', '.jpg': 'image/jpeg', '.webp': 'image/webp', '.json': 'application/json', '.map': 'application/json' };

/**
 * Serves the shot page at `/`, the promo bundle under `/bundle`, and `www` under `/www`, on a free port. Returns the
 * URL of [shot]'s page and a function that stops the server.
 */
export const serveShot = async shot => {
    const routes = [
        ['/bundle/', bundleDir],
        ['/www/', `${root}/www`],
    ];
    const server = createServer(async (request, response) => {
        const path = decodeURIComponent(new URL(request.url, 'http://localhost').pathname);
        try {
            if (path === '/') return response.writeHead(200, { 'Content-Type': 'text/html' }).end(shotPage);
            const [prefix, dir] = routes.find(([prefix]) => path.startsWith(prefix)) ?? [];
            const file = dir && normalize(join(dir, path.slice(prefix.length)));
            if (!file?.startsWith(dir)) throw new Error('not found');
            response.writeHead(200, { 'Content-Type': types[extname(file)] ?? 'application/octet-stream' }).end(await readFile(file));
        } catch {
            response.writeHead(404).end();
        }
    });
    await new Promise(r => server.listen(0, 'localhost', r));
    return { url: `http://localhost:${server.address().port}/?shot=${shot.name}`, close: () => server.close() };
};

/** Clears and creates the frame directory of each segment of [shot], unless only encoding. */
export const segmentDirs = async (shot, ...segments) => {
    const dirs = Object.fromEntries(segments.map(s => [s, `frames/${shot.name}-${mode}-${s}`]));
    if (!encodeOnly) for (const dir of Object.values(dirs)) {
        await rm(dir, { recursive: true, force: true });
        await mkdir(dir, { recursive: true });
    }
    return dirs;
};

// begin frame control lets the studio decide when a frame is drawn; the rest keep drawing on the main thread, in step
const studioArgs = [
    '--deterministic-mode',
    '--enable-begin-frame-control',
    '--run-all-compositor-stages-before-draw',
    '--disable-new-content-rendering-timeout',
    '--disable-threaded-animation',
    '--disable-threaded-scrolling',
    '--disable-checker-imaging',
    '--disable-image-animation-resync',
];

/**
 * Opens a page of [shot]'s `viewport` at the quality scale in a studio browser, where nothing is drawn unless the
 * studio draws it.
 *
 * Until `start()`, the page runs on real time and `until` and `load` draw frames as it goes. From `start()`, time is
 * virtual: each `frame()` advances it by one frame, sends the frame's input, draws, and returns the image. Timers,
 * transitions and animations all keep that time, and virtual time waits while a fetch is pending.
 */
export const openStudio = async shot => {
    const profile = await mkdtemp(join(tmpdir(), 'promo-studio-'));
    const context = await chromium.launchPersistentContext(profile, {
        headless: true,
        // a frame is drawn on the window's own surface, so its size and scale are the window's rather than emulated
        args: [...studioArgs, `--window-size=${shot.viewport.width},${shot.viewport.height}`, `--force-device-scale-factor=${quality.scale}`],
        viewport: null,
    });
    // only a target created with begin frame control takes it
    const [blank] = context.pages();
    const opened = context.waitForEvent('page');
    await (await context.newCDPSession(blank)).send('Target.createTarget', { url: 'about:blank', enableBeginFrameControl: true });
    const page = await opened;
    await blank.close();
    const cdp = await context.newCDPSession(page);

    const interval = 1000 / shot.fps;
    let frameTime = 0;
    let started = false;
    const draw = screenshot => cdp.send('HeadlessExperimental.beginFrame', { frameTimeTicks: frameTime += interval, interval, screenshot });

    // in real time, draws a frame per interval until [task] settles
    const drawing = async task => {
        let done = false;
        const settled = task.finally(() => done = true);
        while (!done) {
            await draw();
            await new Promise(r => setTimeout(r, interval));
        }
        return settled;
    };

    // the mouse's buttons, sent with each event as a browser does
    let pressed = false;
    const mouse = (type, x, y) => cdp.send('Input.dispatchMouseEvent', { type, x, y, button: 'left', buttons: pressed ? 1 : 0, clickCount: type === 'mouseMoved' ? 0 : 1 });

    return {
        page,
        /** Navigates to [url] and waits for the load event. */
        load: url => drawing(page.goto(url)),
        /** Waits until [predicate] holds in the page. */
        until: (predicate, arg) => drawing(page.waitForFunction(predicate, arg, { polling: 50, timeout: 30000 })),
        /** Adds [css] and the drawn pointer to the page and starts virtual time. */
        start: async (css = '') => {
            await page.addStyleTag({ content: pointerCss + css });
            await page.evaluate(svg => document.body.insertAdjacentHTML('beforeend', svg), pointerSvg);
            await cdp.send('Emulation.setVirtualTimePolicy', { policy: 'pause' });
            started = true;
        },
        /**
         * Advances one frame and returns its image. [mouse] moves the real mouse, pressed while `down`, and
         * [pointer] places the drawn one; both are in page pixels.
         */
        frame: async ({ mouse: at, pointer } = {}) => {
            if (!started) throw new Error('the studio has not started');
            const expired = new Promise(r => cdp.once('Emulation.virtualTimeBudgetExpired', r));
            await cdp.send('Emulation.setVirtualTimePolicy', { policy: 'pauseIfNetworkFetchesPending', budget: interval });
            await expired;
            if (pointer) await page.evaluate(({ x, y, scale = 1, opacity = 1 }) => {
                const el = document.getElementById('promo-pointer');
                el.style.opacity = opacity;
                el.style.transform = `translate(${x}px, ${y}px) scale(${scale})`;
            }, pointer);
            // input is handled when the next frame begins, so it is sent without waiting and settles with the draw
            const input = [];
            if (at) {
                input.push(mouse('mouseMoved', at.x, at.y));
                if (at.down !== pressed) {
                    pressed = at.down;
                    input.push(mouse(pressed ? 'mousePressed' : 'mouseReleased', at.x, at.y));
                }
            }
            const { screenshotData } = await draw({ format: 'png' });
            await Promise.all(input);
            return Buffer.from(screenshotData, 'base64');
        },
        close: async () => {
            await context.close();
            await rm(profile, { recursive: true, force: true });
        },
    };
};

/**
 * A function that saves a frame image as the next numbered frame in [dir], at [shot]'s size times the quality
 * scale. Given a `camera`, a rectangle in page pixels with the aspect of the shot, it saves that region.
 */
export const frameWriter = (dir, shot) => {
    const width = shot.size.width * quality.scale;
    const height = shot.size.height * quality.scale;
    let index = 0;
    return async (image, camera) => {
        let frame = sharp(image);
        if (camera) {
            // the image is in device pixels, at the quality scale of the page; rounding keeps the region inside it
            const k = quality.scale;
            const w = Math.min(Math.round(camera.width * k), shot.viewport.width * k);
            const h = Math.min(Math.round(w * height / width), shot.viewport.height * k);
            const left = Math.min(Math.max(Math.round(camera.x * k), 0), shot.viewport.width * k - w);
            const top = Math.min(Math.max(Math.round(camera.y * k), 0), shot.viewport.height * k - h);
            frame = frame.extract({ left, top, width: w, height: h });
        }
        await frame.resize(width, height, { kernel: 'lanczos3' }).png({ compressionLevel: 1 }).toFile(`${dir}/${String(index++).padStart(4, '0')}.png`);
    };
};

/**
 * Encodes [inputs] through [filter] into the next numbered take of [shot], so earlier takes stay for comparison.
 *
 * Each input is a frame directory with its frame rate. Without a [filter], the one input is scaled to the shot's
 * size. `--crf=`, `--preset=` and `--svt=` (extra SVT-AV1 params, colon separated) override the encode.
 */
export const encode = async ({ shot, fps, inputs, filter, seconds }) => {
    const { width, height } = shot.size;
    filter ??= `[0]scale=${width}:${height}:flags=lanczos,settb=1/${fps},setpts=N,fps=${fps}`;
    const stem = `${shot.name}${mode === 'fast' ? '-fast' : ''}`;
    await mkdir('out', { recursive: true });
    const takes = (await readdir('out')).map(f => f.match(new RegExp(`^${stem}-(\\d+)\\.webm$`))?.[1]).filter(Boolean).map(Number);
    const out = `out/${stem}-${Math.max(0, ...takes) + 1}.webm`;
    const result = spawnSync(`${homedir()}/.local/bin/ffmpeg`, [
        '-v', 'error', '-y',
        ...inputs.flatMap(({ dir, rate }) => ['-framerate', String(rate), '-i', `${dir}/%04d.png`]),
        '-filter_complex', filter,
        '-r', String(fps),
        '-c:v', 'libsvtav1', ...(arg('svt') ? ['-svtav1-params', arg('svt')] : []), '-preset', arg('preset') ?? String(quality.preset), '-crf', arg('crf') ?? '32', '-pix_fmt', 'yuv420p10le', '-g', String(Math.ceil(seconds * fps)),
        out,
    ], { stdio: 'inherit', env: { ...process.env, SVT_LOG: '1' } });
    console.log(result.status === 0 ? `wrote ${out}` : 'encode failed');
};
