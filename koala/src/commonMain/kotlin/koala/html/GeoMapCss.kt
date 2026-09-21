package koala.html

// language="CSS"
val GeoMapCss get() = with(GeoMapKey) { """
$Window {
    position: relative;
    overflow: clip;
    width: 100%;
    height: 100%;
    border-radius: inherit;
    isolation: isolate;
}

$Window > * {
    width: 100%;
    height: 100%;
    position: absolute;
    inset: 0;
}

$Overlay {
    position: relative;
    background: radial-gradient(
            circle,
            rgba(0, 0, 0, 0) 0%,
            rgba(.2, .2, .2, 0.25) 60%,
            rgba(.2, .2, .2, 0.50) 100%
    );
    pointer-events: none;
    height: 100%;
    z-index: 1;
}

$Crosshairs {
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

$Panel { width: 100%; }

$FocusPanel {
    position: absolute;
    padding: var(--unit);
    width: 100%;
    bottom: 0;
}

$FocusPanel > * {
    max-width: calc(var(--unit) * 64);
    max-height: calc(var(--unit) * 48);
    margin: 0 auto;
    z-index: 1;
}
""" }
