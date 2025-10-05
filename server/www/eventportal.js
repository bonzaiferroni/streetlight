// Globals the crew can see
let requestSongId = null;
let requestEventId = null;

function startRequest(eventId, songId) {
    requestEventId = eventId;
    requestSongId = songId;

    toggleVisibility("request-details", "request-songs")
}

async function sendRequest() {
    const isJoining = !!getCheckboxValue("join");
    const requesterName = getFieldValue("name");
    const comment = getFieldValue("comment");

    const payload = {
        eventId: requestEventId,
        songId: requestSongId,
        songName: null,          // per yer note: ignore for now
        isJoining: isJoining,
        comment: comment,
        requesterName: requesterName
    };

    await fetchPost("/api/v1/request_box", payload)

    toggleVisibility("request-sent", "request-songs")
}