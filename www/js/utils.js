function toggleVisibility(showId, ...hideIds) {
    document.getElementById(showId)?.classList.remove("display-none");
    hideIds.forEach(id => document.getElementById(id)?.classList.add("display-none"));
}

function getFieldValue(id) {
    return (document.getElementById(id)?.value || "").trim() || null;
}

function getCheckboxValue(id) {
    return document.getElementById(id)?.checked;
}

function getAttributeValue(attribute) {
    return document.querySelector(`[${attribute}]`)?.getAttribute(attribute)
}

async function fetchPost(endpoint, payload) {
    return fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json;charset=utf-8" },
        body: JSON.stringify(payload)
    });
}

function wave(text = "hello world") {
    console.log(text)
}