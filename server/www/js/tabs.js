document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".tabs").forEach(initTabs);
});

function initTabs(root) {
    const DURATION = 200;
    const viewport = root.querySelector(".tabs-viewport");
    if (!viewport) return;

    const buttons = Array.from(root.querySelectorAll(".tabs-header .tabs-button"));
    const panels = Array.from(viewport.querySelectorAll(".tabs-panel"));
    if (panels.length === 0) return;

    // --- Helpers: naming + hash control ---
    const slug = (s) => s
        .toLowerCase()
        .trim()
        .replace(/[_\s]+/g, "-")
        .replace(/[^a-z0-9-]/g, "")
        .replace(/-+/g, "-");

    const tabNameAt = (i) => {
        const btn = buttons[i];
        const pnl = panels[i];
        const fromData = btn?.dataset.name || pnl?.dataset?.name;
        const fromId = pnl?.id || btn?.id;
        const fallback = btn?.textContent || pnl?.getAttribute("aria-label") || `tab-${i + 1}`;
        return slug(fromData || fromId || fallback);
    };

    const names = panels.map((_, i) => tabNameAt(i));

    const readHash = () => {
        const h = decodeURIComponent((location.hash || "").slice(1)).toLowerCase().trim();
        return h || "";
    };

    const setHash = (name) => {
        const url = new URL(location.href);
        url.hash = name ? `#${encodeURIComponent(name)}` : "";
        history.replaceState(null, "", url);
    };

    // --- Initial current (class or #hash) ---
    let current = panels.findIndex((p) => p.classList.contains("is-active"));
    if (current < 0) current = 0;

    const initialHash = readHash();
    const hashIdx = initialHash ? names.indexOf(slug(initialHash)) : -1;
    if (hashIdx >= 0) current = hashIdx;

    // --- Initial apply ---
    panels.forEach((p, i) => {
        p.style.display = i === current ? "block" : "none";
        p.classList.toggle("is-active", i === current);
        p.classList.remove("enter", "exit", "dir-left", "dir-right");
    });
    buttons.forEach((b, i) => b.classList.toggle("is-active", i === current));

    viewport.style.height = panels[current].scrollHeight + "px";
    setTimeout(() => (viewport.style.height = "auto"), DURATION);

    // Ensure URL reflects active tab after init
    setHash(names[current]);

    // --- Click -> swap + hash update ---
    buttons.forEach((btn) => {
        btn.addEventListener("click", () => {
            const next = Number(btn.dataset.tab);
            if (Number.isNaN(next) || next === current || next < 0 || next >= panels.length) return;

            buttons[current]?.classList.remove("is-active");
            buttons[next]?.classList.add("is-active");
            swap(current, next);
            current = next;
            setHash(names[current]);
        });
    });

    // --- React to external hash changes ---
    const onHash = () => {
        const wanted = readHash();
        if (!wanted) return;
        const idx = names.indexOf(slug(wanted));
        if (idx < 0 || idx === current) return;

        buttons[current]?.classList.remove("is-active");
        buttons[idx]?.classList.add("is-active");
        swap(current, idx);
        current = idx;
    };
    window.addEventListener("hashchange", onHash);

    function swap(fromIdx, toIdx) {
        const from = panels[fromIdx];
        const to = panels[toIdx];

        const toDir = toIdx > fromIdx ? "dir-right" : "dir-left";
        const fromDir = toIdx > fromIdx ? "dir-left" : "dir-right";

        viewport.classList.add("animating");

        to.style.display = "block";
        to.classList.remove("is-active", "enter", "exit", "dir-left", "dir-right");
        to.classList.add(toDir, "enter");

        const startH = from.scrollHeight;
        const endH = to.scrollHeight;
        viewport.style.height = startH + "px";
        requestAnimationFrame(() => {
            viewport.style.height = endH + "px";
        });

        from.classList.remove("enter", "exit", "dir-left", "dir-right");
        from.classList.add(fromDir, "exit");

        void to.offsetWidth;

        requestAnimationFrame(() => {
            to.classList.add("is-active");
            to.classList.remove("enter");
        });

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

            from.classList.remove("is-active", "exit", "dir-left", "dir-right");
            from.style.display = "none";

            to.classList.remove("dir-left", "dir-right", "exit");
            to.classList.add("is-active");

            viewport.style.height = "auto";
            viewport.classList.remove("animating");
        }
    }
}
