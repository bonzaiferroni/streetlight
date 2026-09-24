package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.BugEdit

/** The calls of `Api.Bugs`. */
interface BugClient {
    suspend fun report(edit: BugEdit): Outcome<Unit>
}

class BrowserBugClient(private val client: FetchClient): BugClient {
    override suspend fun report(edit: BugEdit) = client.postApi(Api.Bugs.Report, edit)
}
