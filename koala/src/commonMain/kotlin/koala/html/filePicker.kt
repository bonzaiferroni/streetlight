package koala.html

import koala.css.Css

object FilePickerKey {
    val Class = Css("file-picker")
    val DropZone = Css("file-picker-drop-zone")
    val DragOver = Css("dragover")
}

// language="CSS"
val FilePickerCss get() = """
.file-picker-drop-zone {
    border: 2px dashed #888;
    padding: 2rem;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: box-shadow 200ms ease-in-out;
    width: 100%;
    height: 100%;
}

.file-picker-drop-zone.dragover {
    border-color: #fff;
    background: rgba(255, 255, 255, 0.1);
    box-shadow: inset 0 0 0 9999px rgba(255,255,255,.04);
}
"""