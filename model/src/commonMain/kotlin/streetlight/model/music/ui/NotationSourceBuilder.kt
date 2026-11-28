package streetlight.model.music.ui

import streetlight.model.music.Notation

class NotationSourceBuilder(
    private val notations: List<Notation>,
    private val window: StaffWindow,
) {
    private val events = mutableListOf<NotationEvent>()

}