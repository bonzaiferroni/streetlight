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
            p.classList.remove("enter", "exit", "dir-left", "dir-right");
        });
        buttons.forEach((b, i) => b.classList.toggle("is-active", i === current));

        viewport.style.height = panels[current].scrollHeight + "px";
        setTimeout(() => viewport.style.height = "auto", DURATION);

        buttons.forEach(btn => {
            btn.addEventListener("click", () => {
                const next = Number(btn.dataset.tab);
                if (Number.isNaN(next) || next === current || next < 0 || next >= panels.length) return;

                buttons[current]?.classList.remove("is-active");
                buttons[next]?.classList.add("is-active");
                swap(current, next);
                current = next;
            });
        });

        function swap(fromIdx, toIdx) {
            const from = panels[fromIdx];
            const to = panels[toIdx];

            // Determine directions based on relative tab index
            const toDir = toIdx > fromIdx ? "dir-right" : "dir-left";
            const fromDir = toIdx > fromIdx ? "dir-left" : "dir-right";

            // Mark viewport as animating so CSS can overlay panels
            viewport.classList.add("animating");

            // Prepare incoming panel
            to.style.display = "block";
            to.classList.remove("is-active", "enter", "exit", "dir-left", "dir-right");
            to.classList.add(toDir, "enter");

            // Animate viewport height
            const startH = from.scrollHeight;
            const endH = to.scrollHeight;
            viewport.style.height = startH + "px";
            requestAnimationFrame(() => {
                viewport.style.height = endH + "px";
            });

            // Animate outgoing panel
            from.classList.remove("enter", "exit", "dir-left", "dir-right");
            from.classList.add(fromDir, "exit");

            // Force a reflow so the browser registers the 'enter' start state
            // before we activate the new panel and remove 'enter'.
            void to.offsetWidth;

            // Promote both animations in the same frame
            requestAnimationFrame(() => {
                to.classList.add("is-active");
                to.classList.remove("enter");
            });

            // Wait for both transitions to finish (or timeout) before cleanup
            let doneCount = 0;
            const maybeDone = () => {
                doneCount++;
                if (doneCount >= 2) cleanup();
            };

            const onFromEnd = () => { maybeDone(); };
            const onToEnd = () => { maybeDone(); };

            from.addEventListener("transitionend", onFromEnd, { once: true });
            to.addEventListener("transitionend", onToEnd, { once: true });

            const fallback = setTimeout(() => {
                cleanup();
            }, DURATION + 50);

            function cleanup() {
                clearTimeout(fallback);
                from.removeEventListener("transitionend", onFromEnd);
                to.removeEventListener("transitionend", onToEnd);

                // Reset classes and stacking
                from.classList.remove("is-active", "exit", "dir-left", "dir-right");
                from.style.display = "none";

                to.classList.remove("dir-left", "dir-right", "exit");
                to.classList.add("is-active");

                // Reset viewport height and animating flag
                viewport.style.height = "auto";
                viewport.classList.remove("animating");
            }
        }
    }
})();
