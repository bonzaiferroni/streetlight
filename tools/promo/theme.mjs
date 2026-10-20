// The theme shot: the camera starts close on the Background field while the pointer drags the lights, pulls out to
// show the page, closes on Glow Settings while the pointer works the sliders, pulls out again, and returns to the
// field, so the shot loops. Every light and slider ends where it began.
import { buildShots, easeInOut, encode, encodeOnly, fpsOf, frameWriter, lerp, openStudio, progress, segmentDirs, serveShot } from './promo.mjs';

const shot = {
    name: 'theme',
    size: { width: 540, height: 480 },
    viewport: { width: 1080, height: 960 },
    fps: fpsOf(30),
    seconds: 18,
    // the camera's keys: [seconds, frame]; between keys it eases
    camera: [[0, 'background'], [3, 'background'], [4.5, 'page'], [7.5, 'page'], [9, 'glow'], [12, 'glow'], [13.5, 'page'], [16.5, 'page'], [18, 'background']],
    // the margin around a section, as a share of its size
    margin: 0.06,
    // the pointer's size on screen, as a multiple of its drawn size
    pointerSize: 1.1,
};

// the drifting colors and the flame's glow are paced to whole cycles of the loop, so it has no seam
const shotCss = `
    :root {
        --color-flux: hueSpin ${shot.seconds}s linear infinite;
        --glow-background-infinite: glow-background ${shot.seconds / 2}s infinite linear;
    }
    html { scrollbar-width: none; }
`;

const { fps, size, viewport } = shot;
const dirs = await segmentDirs(shot, 'theme');

if (!encodeOnly) {
    await buildShots();
    const server = await serveShot(shot);
    const studio = await openStudio(shot);
    const { page } = studio;
    await studio.load(server.url);
    await studio.until(() => document.querySelector('.glow-control-handle') && document.fonts.status === 'loaded');
    await studio.start(shotCss);

    const section = async title => (await page.getByText(title, { exact: true }).locator('xpath=ancestor::section[1]').boundingBox());
    const centerOf = box => [box.x + box.width / 2, box.y + box.height / 2];
    const frames = {
        background: await section('Background'),
        glow: await section('Glow Settings'),
        page: { x: 0, y: 0, ...viewport },
    };
    // a frame of the shot's aspect around a box, kept inside the page
    const cameraOn = box => {
        const aspect = size.width / size.height;
        const width = Math.min(Math.max(box.width, box.height * aspect) * (1 + 2 * shot.margin), viewport.width);
        const height = width / aspect;
        const [cx, cy] = centerOf(box);
        return {
            x: Math.min(Math.max(cx - width / 2, 0), viewport.width - width),
            y: Math.min(Math.max(cy - height / 2, 0), viewport.height - height),
            width,
        };
    };
    const cameras = Object.fromEntries(Object.entries(frames).map(([key, box]) => [key, cameraOn(box)]));
    // zooms evenly by easing the width on a log scale and the center along with it
    const cameraAt = s => {
        const keys = shot.camera;
        const next = Math.max(keys.findIndex(([at]) => at > s), 1);
        const [from, a] = keys[next - 1];
        const [to, b] = keys[next];
        const t = easeInOut(progress(s, from, to - from));
        const [ca, cb] = [cameras[a], cameras[b]];
        const width = ca.width ** (1 - t) * cb.width ** t;
        const [ax, ay] = [ca.x + ca.width / 2, ca.y + ca.width * size.height / size.width / 2];
        const [bx, by] = [cb.x + cb.width / 2, cb.y + cb.width * size.height / size.width / 2];
        const k = (ca.width - width) / (ca.width - cb.width || 1);
        const [cx, cy] = [ax + (bx - ax) * k, ay + (by - ay) * k];
        return { x: cx - width / 2, y: cy - width * size.height / size.width / 2, width };
    };

    // the pointer's moves: each is [start, end, path], where a path maps progress to a point and says whether it presses
    const handles = await Promise.all((await page.locator('.glow-control-handle').all()).map(async h => centerOf(await h.boundingBox())));
    const field = centerOf(frames.background);
    const sliders = await page.getByText('Glow Settings', { exact: true }).locator('xpath=ancestor::section[1]').locator('input[type=range]').evaluateAll(inputs =>
        inputs.map(input => {
            const r = input.getBoundingClientRect();
            return { x: r.x, y: r.y + r.height / 2, width: r.width, min: Number(input.min), max: Number(input.max), value: Number(input.value) };
        }));
    // Chrome's range thumb is 16px wide, so its center travels the track less 8px at each end
    const thumbAt = (slider, value) => [slider.x + 8 + (value - slider.min) / (slider.max - slider.min) * (slider.width - 16), slider.y];
    const travel = (a, b) => u => ({ at: lerp(a, b, easeInOut(u)), down: false });
    // a light circles once around a point between it and the middle of the field, and comes back where it began
    const circle = (start, turn) => u => {
        const c = [start[0] + (field[0] - start[0]) * 0.45, start[1] + (field[1] - start[1]) * 0.45];
        const angle = turn * 2 * Math.PI * easeInOut(u);
        const [dx, dy] = [start[0] - c[0], start[1] - c[1]];
        return { at: [c[0] + dx * Math.cos(angle) - dy * Math.sin(angle), c[1] + dx * Math.sin(angle) + dy * Math.cos(angle)], down: true };
    };
    // a slider runs to [to] and back to its value
    const slide = (slider, to) => u => {
        const there = Math.sin(Math.PI * easeInOut(u));
        const value = slider.value + (to - slider.value) * there;
        return { at: thumbAt(slider, value), down: true };
    };
    const [rho, beta, gamma] = handles;
    const radius = sliders[1];
    const energy = sliders[3];
    const focus = sliders[8];
    const moves = [
        [0, 2.6, circle(rho, 1)],
        [2.6, 3.2, travel(rho, beta)],
        [3.2, 5.8, circle(beta, -1)],
        [5.8, 6.3, travel(beta, gamma)],
        [6.3, 8.6, circle(gamma, 1)],
        [8.6, 9.4, travel(gamma, thumbAt(radius, radius.value))],
        [9.4, 11.6, slide(radius, radius.max * 0.6)],
        [11.6, 12.1, travel(thumbAt(radius, radius.value), thumbAt(energy, energy.value))],
        [12.1, 14.3, slide(energy, energy.max * 0.15)],
        [14.3, 14.8, travel(thumbAt(energy, energy.value), thumbAt(focus, focus.value))],
        [14.8, 16.4, slide(focus, focus.max * 0.7)],
        [16.4, shot.seconds, travel(thumbAt(focus, focus.value), rho)],
    ];
    const pointerAt = s => {
        const [start, end, path] = moves.find(([start, end]) => s < end) ?? moves.at(-1);
        return path(progress(s, start, end - start));
    };

    const write = frameWriter(dirs.theme, shot);
    for (let i = 0; i < shot.seconds * fps; i++) {
        const s = i / fps;
        const { at: [x, y], down } = pointerAt(s);
        const camera = cameraAt(s);
        const zoom = size.width / camera.width;
        const pointer = { x, y, scale: shot.pointerSize / zoom * (down ? 0.85 : 1) };
        await write(await studio.frame({ mouse: { x, y, down }, pointer }), camera);
    }
    await studio.close();
    server.close();
}

await encode({ shot, fps, inputs: [{ dir: dirs.theme, rate: fps }], seconds: shot.seconds });
