package org.belkevglaz.routing

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.*
import org.belkevglaz.config.*
import org.belkevglaz.features.upsource.*
import org.belkevglaz.features.upsource.model.*


/**
 * Webhooks module.
 */
@ExperimentalSerializationApi
fun Application.webhooks() {

	routing {
		teamcityWebhooks()
		upsourceWebhook()
	}
}

/**
 * Webhook handlers from TeamCity.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 */
fun Routing.teamcityWebhooks() {

	post("/teamcity/complete/{buildId}") {
		println("Received ${call.parameters["buildId"]}")

		val client: UpsourceClient = UpsourceClientImpl("http://base.everytag.ru:7280")
		client.getOpenedReviews(Project("ild", alias = "backend"))

		call.respondText { "Ok" }
	}
}

/**
 * Webhook handlers from Jetbrains Upsource.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 */
@ExperimentalSerializationApi
fun Routing.upsourceWebhook() {

	get("/upsource/hooks") {
		call.respondText { "Upsource hook get Ok" }
	}

	post("/upsource/hooks") {
		println("Received ${call.receive<EventBean>()}")

		call.respondText { "Ok" }
	}


}