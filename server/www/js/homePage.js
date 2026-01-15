document.addEventListener("DOMContentLoaded", initProto);

async function initProto() {
    const root = await protobuf.load("/static/proto/gtfs-realtime.proto");

    // Note the package name: transit_realtime
    const FeedMessage = root.lookupType("transit_realtime.FeedMessage");

    const response = await fetch("/proxy/vehicle-position.pb");
    const buffer = new Uint8Array(await response.arrayBuffer());

    const feed = FeedMessage.decode(buffer);

    const buses = feed.entity
        .filter(e => e.vehicle && e.vehicle.trip && e.vehicle.trip.routeId === "15L" && e.vehicle.position)
        .map(e => new Bus(e.vehicle))

    if (!geoMap) return;

    // stash markers on the map object so we can clear/update them
    if (!geoMap.markers) {
        geoMap.markers = [];
    }

    // remove any old markers
    geoMap.markers.forEach(m => m.remove());
    geoMap.markers = [];

    buses.forEach(bus => {
        const element = document.createElement("div");
        element.className = "map-marker"

        const icon = document.createElement("div");
        const arrow = document.createElement("div");
        icon.className = "map-marker-icon bus-icon";
        arrow.className = "bus-arrow";
        arrow.style.setProperty("--bearing", (bus.bearing - 90 + 360) % 360 + "deg");
        element.appendChild(arrow);
        element.appendChild(icon)

        const marker = new maplibregl.Marker({
            element: element,
            rotationAlignment: "map"
        })
            .setLngLat([bus.position.longitude, bus.position.latitude])
            // .setRotation(bearing + 90)
            .addTo(geoMap);

        geoMap.markers.push(marker);
    });
}

class Bus {
    constructor(data) {
        this.data = data;
    }

    get position() { return this.data.position; }

    get bearing() { return typeof this.position.bearing === "number" ? this.position.bearing : 0;  }
}