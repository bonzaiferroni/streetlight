let geoMap;

document.addEventListener("DOMContentLoaded", () => {
    findAndInitGeoMap(document)
})

function findAndInitGeoMap(root) {
    const mounts = root.querySelectorAll(".map-mount")
    if (mounts.length === 0) return
    if (mounts.length > 1) {
        console.log("Warning: more than one geomap mount found, using first")
    }
    initGeoMap(mounts[0])
}

function initGeoMap(root) {
    const window = createBox("map-window", root)
    const widgetBox = createBox("map-widget", window)
    const overlay = createBox("")

    geoMap = new maplibregl.Map({
        container: "map-widget",
        style: `https://tiles.openfreemap.org/styles/fiord`,
        center: [-104.95, 39.75],
        zoom: 11
    });
    geoMap.addControl(new maplibregl.NavigationControl(), "top-right");
    geoMap.addControl(new maplibregl.FullscreenControl());
}

function createBox(id, parent) {
    const element = document.createElement("div");
    element.id = id
    element.classList.add("box");
    // add to parent if not null
    if (parent) parent.appendChild(element);
    return element;
}