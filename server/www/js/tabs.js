(function () {
    const DURATION = 200;

    document.addEventListener("DOMContentLoaded", () => {
        document.querySelectorAll(".tabs").forEach(initTabs);
    });

    function initTabs(root) {
        const viewport = root.querySelector(".tabs-viewport");
        if (!viewport) return;

        const buttons = Array.from(root.querySelectorAll(".tabs-header .tabs-button"));
        const panels = Array.from(viewport.querySelectorAll(".tabs-panel"));
        if (panels.length === 0) return;

        let current = panels.findIndex(p => p.classList.contains("is-active"));
        if (current < 0) current = 0;

        panels.forEach((p, i) => {
            p.style.display = i === current ? "block" : "none";
            p.classList.toggle("is-active", i === current);
            p.classList.remove("enter-left", "enter-right", "exit-left", "exit-right");
        });
        buttons.forEach((b, i) => b.classList.toggle("is-active", i === current));

        viewport.style.height = panels[current].scrollHeight + "px";
        setTimeout(() => viewport.style.height = "auto", DURATION);

        buttons.forEach(btn => {
            btn.addEventListener("click", () => {
                const next = Number(btn.dataset.tab);
                if (Number.isNaN(next) || next === current || next < 0 || next >= panels.length) return;

                const dir = next > current ? "left" : "right";
                buttons[current]?.classList.remove("is-active");
                buttons[next]?.classList.add("is-active");
                swap(current, next, dir);
                current = next;
            });
        });

        function swap(fromIdx, toIdx, dir) {
            const from = panels[fromIdx];
            const to = panels[toIdx];

            to.style.display = "block";
            to.classList.remove("is-active", "enter-left", "enter-right", "exit-left", "exit-right");
            to.classList.add(dir === "left" ? "enter-right" : "enter-left");

            const startH = from.scrollHeight;
            const endH = to.scrollHeight;
            viewport.style.height = startH + "px";
            requestAnimationFrame(() => {
                viewport.style.height = endH + "px";
            });

            from.classList.remove("enter-left", "enter-right", "exit-left", "exit-right");
            from.classList.add(dir === "left" ? "exit-left" : "exit-right");

            requestAnimationFrame(() => {
                to.classList.add("is-active");
                to.classList.remove("enter-left", "enter-right");
            });

            const done = () => {
                from.classList.remove("is-active", "exit-left", "exit-right");
                from.style.display = "none";
                viewport.style.height = "auto";
            };
            from.addEventListener("transitionend", done, { once: true });
        }
    }
})();
