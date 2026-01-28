document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[lottie]").forEach(element => {
        const path = element.getAttribute("lottie");
        initLottie(element, path)
    });
});

function initLottie(element, path) {
    lottie.loadAnimation({
        container: element,
        renderer: "svg",
        loop: true,
        autoplay: true,
        path: `/www/lottie/${path}.json`
    });
}