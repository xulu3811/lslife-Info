const puppeteer = require('puppeteer');
(async () => {
  const browser = await puppeteer.launch({headless: 'new'});
  const page = await browser.newPage();
  page.on('console', msg => console.log('PAGE LOG:', msg.text()));
  page.on('pageerror', error => console.log('PAGE ERROR:', error.message));
  page.on('requestfailed', request => {
    const errorText = request.failure() ? request.failure().errorText : 'Unknown';
    console.log('REQUEST FAILED:', request.url(), errorText);
  });
  await page.goto('https://mentalhlp.site/admin-web/', {waitUntil: 'networkidle0'});
  await browser.close();
})();
