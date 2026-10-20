const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const CATEGORIES = {
    scripting: new Set([
        'FunctionCall', 'EvaluateScript', 'v8.compile', 'v8.run', 'V8.Execute',
        'RunMicrotasks', 'TimerFire', 'TimerInstall', 'TimerRemove',
        'EventDispatch', 'RequestAnimationFrame', 'FireAnimationFrame',
        'CancelAnimationFrame', 'RequestIdleCallback', 'FireIdleCallback',
        'XHRReadyStateChange', 'XHRLoad', 'MinorGC', 'MajorGC', 'GCEvent',
        'BlinkGC.AtomicPhase', 'V8.GC', 'ProfileCall'
    ]),
    rendering: new Set([
        'RecalculateStyles', 'UpdateLayoutTree', 'ParseAuthorStyleSheet',
        'ScheduleStyleRecalculation', 'InvalidateLayout', 'Layout',
        'LayoutShift', 'HitTest', 'UpdateLayerTree', 'Animation'
    ]),
    painting: new Set([
        'Paint', 'PaintImage', 'PrePaint', 'Rasterize', 'RasterTask',
        'CompositeLayers', 'Commit', 'DecodeImage', 'ResizeImage',
        'DrawFrame', 'DrawLazyPixelRef', 'DecodeLazyPixelRef', 'Layerize'
    ])
};

const COLUMNS = [
    'scripting', 'rendering', 'painting',
    'domContentLoaded', 'load', 'fcp'
];

function categoryOf(name) {
    for (const [category, names] of Object.entries(CATEGORIES)) {
        if (names.has(name)) return category;
    }
    return null;
}

/**
 * Sums self-time per category from a Chrome trace.
 * Self time (duration minus nested children) avoids double-counting,
 * the same way the DevTools summary does it.
 */
function summarizeTrace(traceJson) {
    const events = (traceJson.traceEvents || []).filter(e =>
        e.ph === 'X' &&
        typeof e.dur === 'number' &&
        e.dur > 0 &&
        typeof e.cat === 'string' &&
        e.cat.includes('devtools.timeline')
    );

    const byThread = new Map();
    for (const e of events) {
        const key = `${e.pid}:${e.tid}`;
        if (!byThread.has(key)) byThread.set(key, []);
        byThread.get(key).push(e);
    }

    const totals = { scripting: 0, rendering: 0, painting: 0 };

    for (const threadEvents of byThread.values()) {
        threadEvents.sort((a, b) => a.ts - b.ts || b.dur - a.dur);

        const stack = [];
        const selfTimes = new Map();

        for (const e of threadEvents) {
            while (stack.length > 0) {
                const top = stack[stack.length - 1];
                if (top.ts + top.dur <= e.ts) stack.pop();
                else break;
            }
            if (stack.length > 0) {
                const parent = stack[stack.length - 1];
                selfTimes.set(parent, (selfTimes.get(parent) ?? parent.dur) - e.dur);
            }
            if (!selfTimes.has(e)) selfTimes.set(e, e.dur);
            stack.push(e);
        }

        for (const [event, self] of selfTimes) {
            const category = categoryOf(event.name);
            if (category && self > 0) totals[category] += self / 1000;
        }
    }

    return totals;
}

async function collectPageTimings(page) {
    return page.evaluate(() => {
        const nav = performance.getEntriesByType('navigation')[0];
        const paints = performance.getEntriesByType('paint');
        const fcp = paints.find(p => p.name === 'first-contentful-paint');
        return {
            domContentLoaded: nav ? nav.domContentLoadedEventEnd : null,
            load: nav ? nav.loadEventEnd : null,
            fcp: fcp ? fcp.startTime : null
        };
    });
}

async function runOnce(browser, flow, url, tracePath) {
    const context = await browser.newContext();
    const page = await context.newPage();

    await browser.startTracing(page, {
        path: tracePath,
        categories: ['devtools.timeline', 'disabled-by-default-devtools.timeline']
    });

    let timings = { domContentLoaded: null, load: null, fcp: null };
    try {
        await flow.steps(page, { ...flow, url });
        timings = await collectPageTimings(page);
    } finally {
        await browser.stopTracing();
        await context.close();
    }

    const trace = JSON.parse(fs.readFileSync(tracePath, 'utf8'));
    return { ...summarizeTrace(trace), ...timings };
}

function gitSha() {
    try {
        return execSync('git rev-parse --short HEAD', { cwd: __dirname }).toString().trim();
    } catch {
        return 'unknown';
    }
}

function slug(name) {
    return name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
}

function shuffle(items) {
    const out = items.slice();
    for (let i = out.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [out[i], out[j]] = [out[j], out[i]];
    }
    return out;
}

function appendResults(flow, headless, timestamp, sha, results) {
    const dir = path.join(__dirname, 'results');
    fs.mkdirSync(dir, { recursive: true });
    const file = path.join(dir, 'results.csv');
    const header = `timestamp,sha,benchmark,variant,cache,headless,run,${COLUMNS.join(',')}\n`;
    if (!fs.existsSync(file)) fs.writeFileSync(file, header);

    const rows = results.map(r => [
        timestamp, sha, `"${flow.benchmark}"`, `"${r.variant}"`, flow.cache, headless, r.run,
        ...COLUMNS.map(key => (typeof r[key] === 'number' ? r[key].toFixed(1) : ''))
    ]);

    fs.appendFileSync(file, rows.map(r => r.join(',')).join('\n') + '\n');
}

function median(values) {
    const clean = values
        .filter(v => typeof v === 'number' && !Number.isNaN(v))
        .sort((a, b) => a - b);
    if (clean.length === 0) return null;
    const mid = Math.floor(clean.length / 2);
    return clean.length % 2 === 0 ? (clean[mid - 1] + clean[mid]) / 2 : clean[mid];
}

async function run(flow, conditions, options = {}) {
    const headless = options.headless ?? flow.headless ?? true;
    const traceDir = path.join(__dirname, 'traces', slug(flow.benchmark));
    fs.mkdirSync(traceDir, { recursive: true });

    const sha = gitSha();
    const timestamp = new Date().toISOString();
    const browser = await chromium.launch({ headless });
    const results = [];

    console.log(`${flow.benchmark} ${headless ? 'headless' : 'headed'} — ${flow.runs} runs each\n`);
    for (const c of conditions) console.log(`  ${c.variant.padEnd(20)} ${c.url}`);
    console.log('');

    try {
        for (let i = 1; i <= flow.runs; i++) {
            for (const condition of shuffle(conditions)) {
                const tracePath = path.join(traceDir, `${slug(condition.variant)}-run-${i}.json`);
                const metrics = await runOnce(browser, flow, condition.url, tracePath);
                results.push({ variant: condition.variant, run: i, ...metrics });
                console.log(
                    `  run ${i}  ${condition.variant.padEnd(20)} ` +
                    `scripting ${metrics.scripting.toFixed(0).padStart(5)}ms  ` +
                    `rendering ${metrics.rendering.toFixed(0).padStart(5)}ms  ` +
                    `painting ${metrics.painting.toFixed(0).padStart(5)}ms`
                );
            }
        }
    } finally {
        await browser.close();
    }

    appendResults(flow, headless, timestamp, sha, results);

    for (const condition of conditions) {
        const own = results.filter(r => r.variant === condition.variant);
        console.log(`\n  medians — ${condition.variant}`);
        for (const key of COLUMNS) {
            const value = median(own.map(r => r[key]));
            console.log(`    ${key.padEnd(18)} ${value === null ? 'n/a' : value.toFixed(0) + 'ms'}`);
        }
    }
    console.log(`\n  ${results.length} rows appended to results/results.csv`);
}

module.exports = { run };