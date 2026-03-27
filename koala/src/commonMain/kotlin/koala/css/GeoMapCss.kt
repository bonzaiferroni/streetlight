package koala.css

// language="CSS"
val GeoMapCss get() = """
#map-window {
    position: relative;
    border-radius: 1rem;
    overflow: hidden;
    width: 100%;
    height: 100%;
}

#map-window > * {
    width: 100%;
    height: 100%;
    position: absolute;
    inset: 0;
}

#map-widget {
}

#map-overlay {
    position: relative;
    background: radial-gradient(
            circle,
            rgba(0, 0, 0, 0) 0%,
            rgba(.2, .2, .2, 0.3) 60%,
            rgba(.2, .2, .2, 0.6) 100%
    );
    pointer-events: none;
}

#map-crosshairs {
    width: 12px;
    height: 12px;

    --svg: url("/www/svg/crosshairs.svg");

    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);

    background-color: currentColor;

    -webkit-mask-image: var(--svg);
    -webkit-mask-size: contain;
    -webkit-mask-repeat: no-repeat;
    -webkit-mask-position: center;

    mask-image: var(--svg);
    mask-size: contain;
    mask-repeat: no-repeat;
    mask-position: center;
}

#map-panel {
    width: 100%;
}

/* map marker */
.map-marker.focus {
    z-index: 1;
}

.map-marker__base {
    cursor: pointer;
    position: relative;
    width: 0;
    height: 0;

    transition: transform 200ms ease-in-out;
}

.map-marker__base.scale {
    transform: scale(1);
}

.focus .map-marker__base.scale {
    transform: scale(1.5);
}

/* .marker-icon, .marker-thumb, .marker-label, .marker-bearing */
.map-marker__base > * {
    position: absolute;
    top: 0;
    left: 0;
}

.map-marker__base:hover {
    opacity: 1 !important;
}

.map-marker__body {
    transform: translate(-50%, -50%);
    opacity: 1;
    transition: opacity 200ms ease-in-out;
}

.map-marker__icon {
    width: 24px;
    height: 24px;

    background-image: var(--svg);
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    background-color: transparent; /* important */;
}

.map-marker__bearing {
    width: 40px;
    height: 40px;

    transform: translate(-50%, -50%) rotate(var(--bearing));

    background-image: url(/www/svg/bus-direction.svg);
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    background-color: transparent; /* important */

    transition: transform 1000ms ease-out;
}

.map-marker__thumb {
    --thumb-border-rgb: 255, 255, 255; /* or whatever ye like */

    width: 32px;
    height: 32px;
    max-width: none;

    border-radius: 16px;

    border: 2px solid rgba(var(--thumb-border-rgb), 0.8);

    transition: opacity 200ms ease-in-out, border-radius 200ms ease-in-out;
}

.focus .map-marker__thumb {
    border-radius: 4px;
}

.map-marker__label {
    transform: translate(-50%, calc(50% + .1rem)); /* below body */
    pointer-events: none;
    opacity: 0;
    font-weight: 700;

    max-width: 6rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    transition: opacity 200ms ease-in-out;
}

.focus .map-marker__label {
    opacity: 1;
}

/* event panel */

.map-event-panel {
    display: grid;
    gap: var(--unit-spacing);
    grid-template-columns: 1fr; /* small screens */
    align-items: start;
}

@media (min-width: 48rem) {
    .map-event-panel {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

/* no need for column/break hacks */
.map-panel-card {
    margin: 0;
}

.map-panel-card-grid {
    width: 100%;
    display: grid;
    grid-template-columns: 3rem 1fr 3rem 3rem;
    gap: var(--unit-spacing);
}

.map-panel-card-heading {
    grid-column: 1 / 2;
}

/* map focus.svg */

#map-focus-panel {
    position: absolute;
    left: var(--unit-spacing);
    bottom: var(--unit-spacing);
    margin-right: 4rem;
}

/*glow*/
.marker-glow::before {
    content: "";
    position: absolute;

    left: 50%;
    top: 50%;
    width: 6rem;
    height: 6rem;

    transform: translate(-50%, -50%) scale(1);
    border-radius: 50%;

    pointer-events: none;
    mix-blend-mode: screen;

    opacity: 0.5;

    background: radial-gradient(
            circle at center,
            rgba(120, 200, 255, 0.35) 0%,
            rgba(120, 200, 255, 0.18) 12%,
            rgba(120, 200, 255, 0.08) 24%,
            rgba(120, 200, 255, 0) 70%
    );

    transition:
            opacity 200ms ease,
            transform 200ms ease;

    animation: twinkle-scale 2.4s ease-in-out infinite;
    animation-delay: var(--twinkle-delay, 0s);
}

/* brighter + slightly larger when focused */
.marker-glow.focus::before {
    opacity: 0.75;
    transform: translate(-50%, -50%) scale(1.05);
}

/* utilities */
.above-kite .twinkle-above-kite,
.above-raincloud .twinkle-above-raincloud,
.above-airplane .twinkle-above-airplane {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.65);
    animation: twinkle 2.4s ease-in-out infinite;
    animation-delay: var(--twinkle-delay);
}

.above-kite .twinkle-above-kite > *,
.above-raincloud .twinkle-above-raincloud > *,
.above-airplane .twinkle-above-airplane > * {
    opacity: 0;
}

@keyframes twinkle {
    0%, 100% {
        background-color: rgba(255, 255, 255, 0.45);
    }
    50% {
        background-color: rgba(255, 255, 255, 1);
    }
}

@keyframes twinkle-scale {
    0%, 100% {
        transform: translate(-50%, -50%) scale(1);
    }
    50% {
        transform: translate(-50%, -50%) scale(1.3);
    }
}
"""