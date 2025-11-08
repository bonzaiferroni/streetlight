document.addEventListener("DOMContentLoaded", () => {
    fetch("/static/svg/flame.svg")
        .then(r => r.text())
        .then(svg => {
            document.querySelectorAll(".logo").forEach(el => {
                el.innerHTML = svg;
            });
        })
        .catch(err => console.error("Arr, failed to fetch flame.svg:", err));
});