// Globals the crew can see
var songId = null;
var eventId = null;

function startRequest(evId, sId) {
    eventId = evId;
    songId = sId;

    const songs = document.getElementById("request-songs");
    const details = document.getElementById("request-details");

    if (songs) songs.style.display = "none";
    if (details) {
        details.hidden = false;
        details.style.display = "block";
    }
}

async function sendRequest() {
    const isJoining = !!document.getElementById("join")?.checked;
    const requesterName = (document.getElementById("name")?.value || "").trim() || null;
    const comment = (document.getElementById("comment")?.value || "").trim() || null;

    const payload = {
        eventId: eventId,
        songId: songId,
        songName: null,          // per yer note: ignore for now
        isJoining: isJoining,
        comment: comment,
        requesterName: requesterName
    };

    await fetch("/api/v1/request_box", {
        method: "POST",
        headers: { "Content-Type": "application/json;charset=utf-8" },
        body: JSON.stringify(payload)
    });
}