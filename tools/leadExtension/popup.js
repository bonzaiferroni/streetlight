for (const button of document.querySelectorAll("button[data-kind]")) {
    button.addEventListener("click", () => {
        chrome.runtime.sendMessage({ type: "sendLead", kind: button.dataset.kind });
        window.close();
    });
}
