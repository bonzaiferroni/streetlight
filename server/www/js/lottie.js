document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[lottie]").forEach(el => {
        const path = el.getAttribute("lottie");
        lottie.loadAnimation({
            container: el,
            renderer: "svg",
            loop: true,
            autoplay: true,
            path: path
        });
    });
});