document.addEventListener("DOMContentLoaded", () => {
    findAndInitTabs(document);
});

function findAndInitTabs(parent) {
    parent.querySelectorAll(".tabs").forEach(initTabs);
}

function initTabs(root) {
    const DURATION = 200;
    const STORAGE_KEY = "streetlight.tabValues";
    const viewport = root.querySelector(".tabs-viewport");
    if (!viewport || viewport.isInitialized) return;
    viewport.isInitialized = true;

    const tabId = (root.id || "").trim();

    const buttons = Array.from(root.querySelectorAll(".tabs-header .tabs-button"));
    const panels = Array.from(viewport.querySelectorAll(".tabs-panel"));
    if (panels.length === 0) return;

    const slug = (s) => String(s || "")
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

    const readStorage = () => {
        if (!tabId) return "";
        try {
            const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) || "{}");
            return (stored[tabId] || "").toLowerCase().trim();
        } catch {
            return "";
        }
    };

    const writeStorage = (name) => {
        if (!tabId) return;
        try {
            const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) || "{}");
            stored[tabId] = name;
            localStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
        } catch { /* sail on */ }
    };

    const readQuery = () => {
        if (!tabId) return "";
        const hash = location.hash.startsWith("#") ? location.hash.slice(1) : "";
        const [_, query = ""] = hash.split("?", 2);
        const value = new URLSearchParams(query).get(tabId);
        return (value || "").toLowerCase().trim();
    };

    const readDefault = () => {
        const index = buttons.findIndex(b => b.hasAttribute("is-default"));
        return index === -1 ? -1 : index;
    };

    // Resolve initial tab: URL query wins, then localStorage, then is-default, then first
    let current = panels.findIndex(p => p.classList.contains("is-active"));
    if (current < 0) current = 0;

    const initialWanted = readQuery() || readStorage();
    const wantedIdx = initialWanted ? names.indexOf(slug(initialWanted)) : readDefault();
    if (wantedIdx >= 0) current = wantedIdx;

    // Apply initial state
    buttons[current].dispatchEvent(new CustomEvent("select-tab"));
    panels.forEach((panel, i) => {
        panel.style.display = i === current ? "block" : "none";
        panel.classList.toggle("is-active", i === current);
        panel.classList.remove("enter", "exit", "dir-left", "dir-right");
    });
    buttons.forEach((btn, i) => btn.classList.toggle("is-active", i === current));

    viewport.style.height = panels[current].scrollHeight + "px";
    setTimeout(() => { viewport.style.height = "auto"; }, DURATION);

    writeStorage(names[current]);

    // Tab click handler
    buttons.forEach((btn) => {
        btn.addEventListener("click", () => {
            const next = Number(btn.dataset.tab);
            if (Number.isNaN(next) || next === current || next < 0 || next >= panels.length) return;

            buttons[current]?.classList.remove("is-active");
            buttons[next]?.classList.add("is-active");
            swap(current, next);
            current = next;
            writeStorage(names[current]);
        });
    });

    function swap(fromIdx, toIdx) {
        const from = panels[fromIdx];
        const to = panels[toIdx];
        buttons[toIdx].dispatchEvent(new CustomEvent("select-tab"));

        const lockedScrollX = window.scrollX;
        const lockedScrollY = window.scrollY;

        const restoreScroll = () => {
            if (window.scrollX !== lockedScrollX || window.scrollY !== lockedScrollY) {
                window.scrollTo(lockedScrollX, lockedScrollY);
            }
        };

        const toDir = toIdx > fromIdx ? "dir-right" : "dir-left";
        const fromDir = toIdx > fromIdx ? "dir-left" : "dir-right";

        viewport.classList.add("animating");

        to.style.display = "block";
        to.classList.remove("is-active", "enter", "exit", "dir-left", "dir-right");
        to.classList.add(toDir, "enter");

        const startH = from.scrollHeight;
        const endH = to.scrollHeight;

        viewport.style.height = startH + "px";
        restoreScroll();

        requestAnimationFrame(() => {
            viewport.style.height = endH + "px";
            restoreScroll();
        });

        from.classList.remove("enter", "exit", "dir-left", "dir-right");
        from.classList.add(fromDir, "exit");

        void to.offsetWidth;

        requestAnimationFrame(() => {
            to.classList.add("is-active");
            to.classList.remove("enter");
            restoreScroll();
        });

        let cleanedUp = false;
        let doneCount = 0;

        const onEnd = () => {
            if (++doneCount >= 2) cleanup();
        };

        from.addEventListener("transitionend", onEnd, { once: true });
        to.addEventListener("transitionend", onEnd, { once: true });

        const fallback = setTimeout(cleanup, DURATION + 50);

        function cleanup() {
            if (cleanedUp) return;
            cleanedUp = true;

            clearTimeout(fallback);
            from.removeEventListener("transitionend", onEnd);
            to.removeEventListener("transitionend", onEnd);

            from.classList.remove("is-active", "exit", "dir-left", "dir-right");
            from.style.display = "none";

            to.classList.remove("dir-left", "dir-right", "exit");
            to.classList.add("is-active");

            viewport.style.height = "auto";
            viewport.classList.remove("animating");

            restoreScroll();
            requestAnimationFrame(restoreScroll);
            setTimeout(restoreScroll, 0);
        }
    }
}