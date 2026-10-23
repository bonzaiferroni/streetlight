const pw = require('/home/starfox/projects/streetlight/tools/benchmark/node_modules/playwright');
const crypto = require('crypto');
(async () => {
  for (const name of ['chromium', 'firefox', 'webkit']) {
    const b = await pw[name].launch();
    const p = await b.newPage();
    await p.goto('http://localhost:8765/index.html');
    await p.waitForTimeout(800);
    const shots = [];
    for (let i = 0; i < 3; i++) { shots.push(crypto.createHash('md5').update(await p.locator('#a').screenshot()).digest('hex')); await p.waitForTimeout(370); }
    console.log(name, new Set(shots).size > 1 ? 'ANIMATES' : 'STATIC', shots.map(s => s.slice(0, 6)).join(' '));
    await b.close();
  }
})();
