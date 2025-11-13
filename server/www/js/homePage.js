document.addEventListener("DOMContentLoaded", initProto)

async function initProto() {
    const root = await protobuf.load("/static/proto/gtfs-realtime.proto");

    // Note the package name: transit_realtime
    const FeedMessage = root.lookupType("transit_realtime.FeedMessage");

    const response = await fetch("/static/proto/VehiclePosition.pb");
    const buffer = new Uint8Array(await response.arrayBuffer());

    const feed = FeedMessage.decode(buffer);

    const vehicles15L = feed.entity
        .filter(e => e.vehicle && e.vehicle.trip && e.vehicle.trip.routeId === "15L")
        .map(e => e.vehicle)
        .filter(v => v.position); // must have lat/lon

    const geojson = {
        type: "FeatureCollection",
        features: vehicles15L.map(v => ({
            type: "Feature",
            geometry: {
                type: "Point",
                coordinates: [v.position.longitude, v.position.latitude]
            },
            properties: {
                vehicleId: v.vehicle?.id ?? "",
                tripId: v.trip?.tripId ?? ""
            }
        }))
    };

    if (!geoMap) return;

    const addLayer = () => {
        if (!geoMap.getSource("route-15L")) {
            geoMap.addSource("route-15L", {
                type: "geojson",
                data: geojson
            });
            geoMap.addLayer({
                id: "route-15L",
                type: "circle",
                source: "route-15L",
                paint: {
                    "circle-radius": 4,
                    "circle-color": "#ff5500"
                }
            });
        } else {
            geoMap.getSource("route-15L").setData(geojson);
        }
    };

    if (geoMap.isStyleLoaded()) {
        addLayer();
    } else {
        geoMap.on("load", addLayer);
    }
}