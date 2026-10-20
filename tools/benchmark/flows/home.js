module.exports = {
    benchmark: 'Uncached Home Load',
    cache: 'cold',
    url: 'http://localhost:8080/',
    runs: 5,
    headless: true,
    readySelector: '#home-box',
    settleMs: 2000,

    async steps(page, flow) {
        await page.goto(flow.url, { waitUntil: 'load' });
        await page.waitForSelector(flow.readySelector, { timeout: 15000 });
        await page.waitForTimeout(flow.settleMs);
    }
};