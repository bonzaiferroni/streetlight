document.addEventListener("DOMContentLoaded", initGeoMap)

function initGeoMap() {
    const map = new maplibregl.Map({
        container: "geoMap",
        style: `https://tiles.openfreemap.org/styles/fiord`,
        center: [-104.95, 39.75],
        zoom: 11
    });
    map.addControl(new maplibregl.NavigationControl(), "top-right");
}