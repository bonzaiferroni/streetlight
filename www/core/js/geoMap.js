let geoMap;

document.addEventListener("DOMContentLoaded", initGeoMap)

function initGeoMap() {
    geoMap = new maplibregl.Map({
        container: "geo-map",
        style: `https://tiles.openfreemap.org/styles/fiord`,
        center: [-104.95, 39.75],
        zoom: 11
    });
    geoMap.addControl(new maplibregl.NavigationControl(), "top-right");
    geoMap.addControl(new maplibregl.FullscreenControl());
}