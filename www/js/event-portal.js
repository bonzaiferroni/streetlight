let requestSongId = null;

function startRequest(songId) {
    requestSongId = songId;

    toggleVisibility("request-details", "request-songs")
}

async function sendRequest() {
    const eventId = getAttributeValue("event-id")
    console.log(eventId)
    const isJoining = !!getCheckboxValue("join");
    const requesterName = getFieldValue("name");
    const comment = getFieldValue("comment");

    const payload = {
        eventId: eventId,
        songId: requestSongId,
        songName: null,
        isJoining: isJoining,
        comment: comment,
        requesterName: requesterName
    };

    console.log(payload)

    await fetchPost("/api/v1/request_box", payload)

    toggleVisibility("request-sent", "request-details")
}