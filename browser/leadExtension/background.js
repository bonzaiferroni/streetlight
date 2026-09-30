const menuId = "add-to-streetlight";
const locationItemId = "location";

chrome.runtime.onInstalled.addListener(() => {
    chrome.contextMenus.create({
        id: menuId,
        title: "Add to Streetlight",
        contexts: ["page"],
    });
    chrome.contextMenus.create({
        id: locationItemId,
        parentId: menuId,
        title: "Location",
        contexts: ["page"],
    });
});

chrome.contextMenus.onClicked.addListener((info) => {
    if (info.menuItemId === locationItemId) {
        console.log("sending location");
    }
});
