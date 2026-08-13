const HELM = '#site-helm-button > button';
const LOGO = '#app-overlay div.logo-shadow-box > div';
const EVENT = '#post-layout div:nth-of-type(17) h5';

module.exports = {
    benchmark: 'Theme Transition',
    cache: 'cold',
    url: 'http://localhost:8080/',
    runs: 5,
    headless: true,
    readySelector: '#home-box',
    settleMs: 2000,

    async steps(page, flow) {
        const pause = () => page.waitForTimeout(flow.settleMs);

        await page.goto(flow.url, { waitUntil: 'load' });
        await page.waitForSelector(flow.readySelector, { timeout: 15000 });

        await page.click(HELM);
        await pause();

        await page.click(LOGO);
        await pause();

        await page.click(EVENT);
        await pause();

        await page.click(HELM);
        await pause();

        await page.click(LOGO);
        await pause();
    }
};