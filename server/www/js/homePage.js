document.addEventListener("DOMContentLoaded", initProto);

async function initProto() {
    const root = await protobuf.load("/static/proto/gtfs-realtime.proto");

    // Note the package name: transit_realtime
    const FeedMessage = root.lookupType("transit_realtime.FeedMessage");

    const response = await fetch("/proxy/vehicle-position.pb");
    const buffer = new Uint8Array(await response.arrayBuffer());

    const feed = FeedMessage.decode(buffer);

    const vehicles15L = feed.entity
        .filter(e => e.vehicle && e.vehicle.trip && e.vehicle.trip.routeId === "15L")
        .map(e => e.vehicle)
        .filter(v => v.position); // must have lat/lon

    if (!geoMap) return;

    // stash markers on the map object so we can clear/update them
    if (!geoMap.route15LMarkers) {
        geoMap.route15LMarkers = [];
    }

    // remove any old markers
    geoMap.route15LMarkers.forEach(m => m.remove());
    geoMap.route15LMarkers = [];

    vehicles15L.forEach(v => {
        const bearing = typeof v.position.bearing === "number" ? v.position.bearing : 0;

        const el = document.createElement("div");
        el.className = "geo-marker " + (bearing > 180 ? "bus-marker-left" : "bus-marker");

        const marker = new maplibregl.Marker({
            element: el,
            rotationAlignment: "map"
        })
            .setLngLat([v.position.longitude, v.position.latitude])
            // .setRotation(bearing + 90)
            .addTo(geoMap);

        geoMap.route15LMarkers.push(marker);
    });
}