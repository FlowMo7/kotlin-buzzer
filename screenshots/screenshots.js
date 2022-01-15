const puppeteer = require('puppeteer');

const browserWidth = 500;
const browserHeight = 600;
const baseUrl = 'https://buzzer.moetz.dev';

function delay(time) {
    return new Promise(function(resolve) {
        setTimeout(resolve, time)
    });
}

async function createBrowser() {
    return puppeteer.launch({
        headless: true,
        args: [
            "--disable-gpu",
            "--disable-dev-shm-usage",
            "--disable-setuid-sandbox",
            "--no-sandbox",
            "--window-size=" + browserWidth + "," + browserHeight,
        ],
        defaultViewport: {
            width: browserWidth,
            height: browserHeight
        }
    });
}

async function makeScreenshot(url, screenshotFilename) {
    const browser = await createBrowser()
    const page = await browser.newPage();
    await page.goto(url);
    await delay(2000);
    const indexScreenshot = await page.screenshot({path: '/screenshots/' + screenshotFilename});

    await page.close();
    await browser.close();
}

async function makeLobbyScreenshots() {
    const browser = await createBrowser()
    const pageHost = await browser.newPage();
    await pageHost.goto(baseUrl + '/host/abc123');

    const pageParticipant1 = await browser.newPage();
    await pageParticipant1.goto(baseUrl + '/lobby/abc123?nickname=Max');

    const pageParticipant2 = await browser.newPage();
    await pageParticipant2.goto(baseUrl + '/lobby/abc123?nickname=Eva');

    const pageParticipant3 = await browser.newPage();
    await pageParticipant3.goto(baseUrl + '/lobby/abc123?nickname=Michael');

    await delay(1000);

    await pageParticipant2.click('#buzzer_button')

    await delay(1000);

    await pageHost.screenshot({path: '/screenshots/host_one_buzzed.png'});
    await pageParticipant2.screenshot({path: '/screenshots/participant_buzzed.png'});
    await pageParticipant1.screenshot({path: '/screenshots/participant_not_buzzed.png'});

    await pageParticipant3.click('#buzzer_button')
    await delay(1000);
    await pageHost.screenshot({path: '/screenshots/host_two_buzzed.png'});

    await pageHost.close();
    await pageParticipant1.close();
    await pageParticipant2.close();
    await pageParticipant3.close();
    await browser.close();
}

(async () => {
    await makeScreenshot(baseUrl, 'index.png');
    await makeScreenshot(baseUrl + '/join/abc123', 'join.png');
    await makeLobbyScreenshots();
})();