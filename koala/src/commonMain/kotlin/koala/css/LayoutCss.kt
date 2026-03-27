package koala.css

// language="CSS"
val LayoutCss get() = """
.column {
    display: flex;
    flex-direction: column;
    min-width: 0;
    gap: var(--unit-spacing);
}

.row {
    display: flex;
    flex-direction: row;
    min-width: 0;

    gap: var(--unit-spacing);
}

.row > .row,
.row > .column {
    width: auto;
}

.card {
    display: flex;
    flex-direction: column;

    gap: var(--unit-spacing);
    border-radius: calc(var(--unit-spacing) * 2);
    background-color: var(--card-bg);
    padding: var(--unit-spacing);
}

.box {
    display: grid;
}

.box > * {
    grid-area: 1 / 1 / 2 / 2;
}

.box > .center {
    place-self: center;
}

.box.place-center-items {
    place-items: center;
}

.flow-block {
    width: 100%;
    min-width: 0;
}

.items-block {
    width: 100%;
    position: relative;
}

.items-block > * {
    width: 100%;
    position: absolute;
}

@media (min-width: 600px) {
    .query-medium-row {
        flex-direction: row;
    }

    .query-medium-column {
        flex-direction: column;
    }

    .query-medium-row > .column {
        width: auto;
    }

    .query-medium-row-reverse {
        flex-direction: row-reverse;
        justify-content: flex-end;
    }

    .query-medium-flex-1 {
        flex: 1;
    }

    .query-medium-flex-2 {
        flex: 2;
    }

    .query-medium-flex-3 {
        flex: 3;
    }

    .query-medium-flex-4 {
        flex: 4;
    }
}

@media (min-width: 768px) {
    .query-large-row {
        flex-direction: row;
    }

    .query-large-column {
        flex-direction: column;
    }

    .query-large-row > .column {
        width: auto;
    }

    .query-large-row-reverse {
        flex-direction: row-reverse;
        justify-content: flex-end;
    }

    .query-large-flex-1 {
        flex: 1;
    }

    .query-large-flex-2 {
        flex: 2;
    }

    .query-large-flex-3 {
        flex: 3;
    }

    .query-large-flex-4 {
        flex: 4;
    }
}
"""