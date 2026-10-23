// Usage: node sheet.js out.png label1 label2 ...
// Renders each label: static large, then animated frames across one full loop period.
// Reports: frame0 == static large, frame(T) == frame0 (seamless), XML parse errors.
const pw = require('/home/starfox/projects/streetlight/tools/benchmark/node_modules/playwright');
const fs = require('fs');
const crypto = require('crypto');
const dir = '/home/starfox/projects/streetlight/www/icon';
const [out, ...labels] = process.argv.slice(2);
const SZ = +(process.env.SZ || 72);
const FRAMES = +(process.env.FRAMES || 8);
const gcd = (a, b) => b ? gcd(b, a % b) : a;
const ms = d => d.endsWith('ms') ? parseFloat(d) : parseFloat(d) * 1000;
function period(src) {
  const durs = [...src.matchAll(/\bdur="([^"]+)"/g)].map(m => Math.round(ms(m[1])));
  if (!durs.length) return 0;
  return durs.reduce((a, b) => a / gcd(a, b) * b);
}
const strip = s => s.replace(/<\?xml[^>]*>/, '');
(async () => {
  const b = await pw.chromium.launch();
  const p = await b.newPage({ deviceScaleFactor: 1 });
  let html = '<!doctype html><body style="margin:8px;background:#fff;color:#000;font:11px monospace">';
  const info = [];
  for (const l of labels) {
    const large = fs.readFileSync(`${dir}/${l}-large.svg`, 'utf8');
    const animPath = `${dir}/${l}-animated.svg`;
    const anim = fs.existsSync(animPath) ? fs.readFileSync(animPath, 'utf8') : null;
    const T = anim ? period(anim) : 0;
    info.push({ l, T, hasAnim: !!anim });
    html += `<div style="display:flex;gap:6px;align-items:center;margin-bottom:6px"><span style="width:150px">${l} ${T}ms</span>`;
    html += `<div class="cell" id="${l}-s" data-t="-1" style="width:${SZ}px;height:${SZ}px">${strip(large).replace('<svg', `<svg width="${SZ}" height="${SZ}"`)}</div>`;
    if (anim) for (let k = 0; k <= FRAMES; k++) {
      const t = T * k / FRAMES / 1000;
      html += `<div class="cell" id="${l}-${k}" data-t="${t}" style="width:${SZ}px;height:${SZ}px;outline:1px solid #ddd">${strip(anim).replace('<svg', `<svg width="${SZ}" height="${SZ}"`)}</div>`;
    }
    html += '</div>';
  }
  html += '</body>';
  const errors = [];
  p.on('console', m => { if (m.type() === 'error') errors.push(m.text()); });
  await p.setContent(html);
  await p.evaluate(() => {
    for (const c of document.querySelectorAll('.cell')) {
      const svg = c.querySelector('svg'); const t = parseFloat(c.dataset.t);
      if (t >= 0) { svg.pauseAnimations(); svg.setCurrentTime(t); }
    }
  });
  await p.waitForTimeout(100);
  await p.screenshot({ path: out, fullPage: true });
  const shot = async id => (await p.locator('#' + CSS_escape(id)).screenshot()).toString('base64');
  const diff = (a, b) => p.evaluate(async ([a, b]) => {
    const load = src => new Promise(r => { const i = new Image(); i.onload = () => r(i); i.src = 'data:image/png;base64,' + src; });
    const [ia, ib] = await Promise.all([load(a), load(b)]);
    const px = i => { const c = document.createElement('canvas'); c.width = i.width; c.height = i.height; const x = c.getContext('2d'); x.drawImage(i, 0, 0); return x.getImageData(0, 0, i.width, i.height).data; };
    const da = px(ia), db = px(ib); let n = 0;
    for (let k = 0; k < da.length; k += 4) if (Math.abs(da[k] - db[k]) > 48) n++;
    return n;
  }, [a, b]);
  function CSS_escape(s) { return s.replace(/([^a-zA-Z0-9_-])/g, '\\$1'); }
  for (const { l, T, hasAnim } of info) {
    if (!hasAnim) { console.log(`${l}: NO ANIMATED FILE`); continue; }
    const s = await shot(`${l}-s`), f0 = await shot(`${l}-0`), fT = await shot(`${l}-${FRAMES}`);
    let motion = 0; for (let k = 1; k < FRAMES; k++) motion = Math.max(motion, await diff(f0, await shot(`${l}-${k}`)));
    const flags = [];
    const d0 = await diff(s, f0), dT = await diff(f0, fT);
    if (d0 > 2) flags.push(`FRAME0!=STATIC(${d0}px)`);
    if (dT > 2) flags.push(`SEAM(${dT}px)`);
    if (motion < 4) flags.push('NO-MOTION');
    if (!T) flags.push('NO-DUR');
    console.log(`${l}: ${T}ms motion=${motion}px ${flags.length ? flags.join(' ') : 'ok'}`);
  }
  if (errors.length) console.log('console errors:', errors);
  await b.close();
})();
