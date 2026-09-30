# Module browser.leadExtension

## Introduction

A browser extension that sends the page in view to Streetlight as a lead, from the page's context menu.

## Structure

The extension is plain JavaScript with no build step, loaded from `browser/leadExtension` as it stands.

One Manifest V3 manifest serves Firefox and Chrome, from version 121 of each. `background` declares `background.js` under both `scripts` and `service_worker`: Firefox runs it as an event page and Chrome as a service worker. Scripts use the `chrome.*` namespace, which both browsers provide.

`background.js` holds the context menu: a parent item, "Add to Streetlight", with one child item per kind of lead. A menu item is created in `runtime.onInstalled` and handled in `contextMenus.onClicked` by its id.

The background script's console is the extension's own, not the page's.

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
