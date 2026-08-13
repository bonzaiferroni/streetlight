const HELM = '#site-helm-button > button';
const LOGO = '#app-overlay div.logo-shadow-box > div';
const EVENT_TEXT = 'Swallow Hill Music';

module.exports = {
    benchmark: 'Default',
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

        await page.locator('#post-layout h5', { hasText: EVENT_TEXT }).first().click();
        await pause();

        await page.click(HELM);
        await pause();

        await page.click(LOGO);
        await pause();
    }
};