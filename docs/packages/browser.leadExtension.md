# Module browser.leadExtension

## Introduction

A browser extension that sends the page in view to Streetlight as a lead.

## Structure

The extension is plain JavaScript with no build step, loaded from `browser/leadExtension` as it stands. Its icon, `icons/foxicon.png`, is the PNG inside the site's `www/icon/foxicon.ico`, and serves the extension and its toolbar button at every size.

One Manifest V3 manifest serves Firefox and Chrome, from version 121 of each. `background` declares `background.js` under both `scripts` and `service_worker`: Firefox runs it as an event page and Chrome as a service worker. Scripts use the `chrome.*` namespace, which both browsers provide.

A lead is sent from two places: the toolbar button's popup and the page's context menu. Each offers one choice per kind of lead, identified by the kind's name, and both hand it to `sendLead` in `background.js`, which does all sending. `leadKinds` in `background.js` maps each kind to its menu title and its `LeadType`; a new kind is an entry there and a button in `popup.html`, and the context menu builds its items from `leadKinds`.

| Source | Choice | Hands off by |
|---|---|---|
| Popup (`popup.html`, `popup.js`) | A `button` with `data-kind` | `runtime.sendMessage` of `{ type: "sendLead", kind }` |
| Context menu | An item under "Add to Streetlight", its id the kind, created in `runtime.onInstalled` | `contextMenus.onClicked` |

The popup closes once it hands off. Logs from sending land in the background script's console, the extension's own, not the page's.

## Sending

`sendLead` reads the active tab's `location.href` and rendered `document.documentElement.outerHTML` through `scripting.executeScript`, under `activeTab`, and posts them as a `StarLead` to `Api.Stars.CreateLead`, with the html as its `content`. The request carries the browser's Streetlight session as an `Authorization: Bearer` header, its token read from the `session_token` cookie through `cookies.getAll` by the host's domain, never by its `http` url, which leaves out the cookie as `secure`, so the user is signed in to Streetlight as an admin in the same browser. Nothing is sent when the cookie is missing. Only the response status is read, and it is logged.

The Streetlight host is listed in `host_permissions` with any scheme and port, as `*://localhost/*`. Firefox grants a host permission only when the user allows it on the extension's permissions page.

## Workflows

Loading in Firefox:

1. Open `about:debugging#/runtime/this-firefox`.
2. Choose "Load Temporary Add-on" and select `browser/leadExtension/manifest.json`.
3. "Inspect" on the extension opens its console.

Loading in Chrome:

1. Open `chrome://extensions` and turn on developer mode.
2. Choose "Load unpacked" and select `browser/leadExtension`.
3. "Inspect views" on the extension opens its console.

A change to `background.js` takes effect after "Reload" on the extension.
