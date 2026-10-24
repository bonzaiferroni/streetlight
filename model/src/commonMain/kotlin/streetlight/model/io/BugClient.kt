package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.postApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.BugEdit

/** The calls of `Api.Bugs`. */
class BugClient(private val transport: ApiTransport) {
    suspend fun report(edit: BugEdit): Outcome<Unit> = transport.postApi(Api.Bugs.Report, edit)
}
