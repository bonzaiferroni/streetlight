const menuId = "add-to-streetlight";
const streetlightUrl = "http://localhost:8080";
const createLeadUrl = `${streetlightUrl}/api/v1/stars/create-lead`;
const sessionCookieName = "session_token";

/** Each kind of lead the extension sends, by its kind's name, with its menu title and its `LeadType`. */
const leadKinds = {
    location: { title: "Location", leadType: "Location" },
    event: { title: "Event", leadType: "Event" },
};

chrome.runtime.onInstalled.addListener(() => {
    chrome.contextMenus.create({
        id: menuId,
        title: "Add to Streetlight",
        contexts: ["page"],
    });
    for (const [kind, { title }] of Object.entries(leadKinds)) {
        chrome.contextMenus.create({
            id: kind,
            parentId: menuId,
            title,
            contexts: ["page"],
        });
    }
});

chrome.contextMenus.onClicked.addListener((info) => {
    sendLead(info.menuItemId);
});

chrome.runtime.onMessage.addListener((message) => {
    if (message.type === "sendLead") sendLead(message.kind);
});

async function sendLead(kind) {
    const leadKind = leadKinds[kind];
    if (!leadKind) return;
    try {
        const [cookie] = await chrome.cookies.getAll({ domain: new URL(streetlightUrl).hostname, name: sessionCookieName });
        if (!cookie) {
            console.log(`${kind} lead: not signed in to Streetlight`);
            await logCookieDiagnostic();
            return;
        }
        const page = await readActivePage();
        console.log(`sending ${kind}: ${page.url}`);
        const response = await fetch(createLeadUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${cookie.value}`,
            },
            body: JSON.stringify({ url: page.url, leadType: leadKind.leadType, content: page.content }),
        });
        console.log(`${kind} lead: ${response.status} ${response.statusText}`);
    } catch (e) {
        console.error(`${kind} lead failed`, e);
    }
}

/** Logs whether the host is permitted and every session cookie visible to the extension, without its value. */
async function logCookieDiagnostic() {
    const permitted = await chrome.permissions.contains({ origins: ["*://localhost/*"] });
    console.log(`diagnostic: host permitted: ${permitted}`);
    for (const store of await chrome.cookies.getAllCookieStores()) {
        const cookies = await chrome.cookies.getAll({ name: sessionCookieName, storeId: store.id });
        console.log(`diagnostic: store ${store.id}: ${cookies.length} ${sessionCookieName} cookie(s)`);
        for (const { value, ...cookie } of cookies) console.log("diagnostic:", cookie);
    }
}

/** The url and rendered html of the page in the active tab. */
async function readActivePage() {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    const [result] = await chrome.scripting.executeScript({
        target: { tabId: tab.id },
        func: () => ({ url: location.href, content: document.documentElement.outerHTML }),
    });
    return result.result;
}
