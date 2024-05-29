package streetlight.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpHeaders.Date
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import streetlight.model.User
import streetlight.server.data.area.AreaService
import streetlight.server.data.area.areaRouting
import streetlight.server.data.event.EventInfoService
import streetlight.server.data.event.EventService
import streetlight.server.data.event.eventInfoRouting
import streetlight.server.data.event.eventRouting
import streetlight.server.data.location.LocationService
import streetlight.server.data.location.locationRouting
import streetlight.server.data.user.UserService
import streetlight.server.data.user.userRouting
import java.util.Date

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        userRouting(UserService())
        locationRouting(LocationService())
        areaRouting(AreaService())
        eventRouting(EventService())
        eventInfoRouting(EventInfoService())

        post("/login") {
            val audience = "http://localhost:8080/"
            val issuer = "http://localhost:8080/"
            val secret = "secret"

            val user = call.receive<User>()
            if (user.name != "admin" || user.password != "admin") {
                call.respond(HttpStatusCode.Unauthorized, "Invalid user")
                return@post
            }

            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)

                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                // .withClaim("username", user.name)
                .sign(Algorithm.HMAC256(secret))
            call.respond(hashMapOf("token" to token))
        }
    }
}
