// Usage: node blip.js label [stepMs]
// Steps through one loop of label-animated.svg and prints the times where the frame jumps (PNG size changes sharply),
// to find blinks a sheet's 8 samples miss, such as two layers swapping in the same instant.
const pw = require('/home/starfox/projects/streetlight/tools/benchmark/node_modules/playwright');
const fs = require('fs');
const [label, stepArg] = process.argv.slice(2);
const step = Number(stepArg || 20);
const svg = fs.readFileSync(`/home/starfox/projects/streetlight/www/icon/${label}-animated.svg`, 'utf8');
const gcd = (a, b) => b ? gcd(b, a % b) : a;
const durs = [...svg.matchAll(/\bdur="([\d.]+)s"/g)].map(m => Math.round(parseFloat(m[1]) * 1000));
const period = durs.reduce((a, b) => a / gcd(a, b) * b);
(async () => {
  const b = await pw.chromium.launch(); const p = await b.newPage();
  await p.setContent(`<body style="margin:0;background:#fff">${svg.replace('<svg', '<svg id="s" width="96" height="96"')}</body>`);
  let prev = null;
  for (let ms = 0; ms <= period; ms += step) {
    await p.evaluate(t => { const s = document.getElementById('s'); s.pauseAnimations(); s.setCurrentTime(t); }, ms / 1000);
    const n = (await p.screenshot({ clip: { x: 0, y: 0, width: 96, height: 96 } })).length;
    if (prev !== null && Math.abs(n - prev) > 400) console.log(`${ms}ms: ${prev} -> ${n}`);
    prev = n;
  }
  await b.close();
  console.log(`checked ${period}ms in ${step}ms steps`);
})();
