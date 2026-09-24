package streetlight.web.io

import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.model.AccountUpgradeRequest
import kampfire.model.EmailChange
import kampfire.model.GeoPoint
import kampfire.model.GeoRect
import kampfire.model.LoginRequest
import kampfire.model.Outcome
import kampfire.model.PasswordChange
import kampfire.model.PasswordVerification
import kampfire.model.SignUpRequest
import kampfire.model.Url
import koala.Image
import koala.model.DocId
import koala.model.DocTableItem
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*
import streetlight.model.writeCursor
import web.sockets.WebSocket
import web.sse.EventSource
import kotlin.uuid.Uuid

/** The calls of `Api.Tasks`. */
interface TaskClient {
    suspend fun readUserTasks(): Outcome<List<TaskContent>>
    suspend fun readReview(taskId: TaskId): Outcome<TaskContent>
}

class BrowserTaskClient(private val client: FetchClient): TaskClient {
    override suspend fun readUserTasks() = client.getApi(Api.Tasks.ReadStarTasks)
    override suspend fun readReview(taskId: TaskId) = client.getApi(Api.Tasks.ReadStarTask, taskId)
}
