package streetlight.web.io

import kampfire.model.Ok
import kampfire.model.Outcome
import streetlight.model.data.BugEdit

class TestBugClient(
    private val onReport: (BugEdit) -> Outcome<Unit> = { Ok(Unit) },
): BugClient {

    val sent = mutableListOf<BugEdit>()

    override suspend fun report(edit: BugEdit): Outcome<Unit> {
        sent.add(edit)
        return onReport(edit)
    }
}
