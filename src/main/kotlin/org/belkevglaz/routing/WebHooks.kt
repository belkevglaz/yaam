package org.belkevglaz.routing

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.*
import org.belkevglaz.config.*
import org.belkevglaz.features.upsource.*
import org.belkevglaz.features.upsource.model.*
import org.koin.ktor.ext.*


/**
 * Webhooks module.
 */
@ExperimentalSerializationApi
fun Application.webhooks() {

	routing {
		teamcityWebhook()
		upsourceWebhook()
	}
}

/**
 * Webhook handlers from TeamCity.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 */
fun Routing.teamcityWebhook() {

	post("/teamcity/complete/{buildId}") {
		println("Received ${call.parameters["buildId"]}")
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

	val client: UpsourceClient by inject()
	val service: UpsourceService by inject()


	get("/upsource/hooks") {
		call.respondText { "Upsource hook get Ok" }
		client.getOpenedReviews(Project("ild", alias = "backend"))
	}

	post("/upsource/hooks") {
		val event = call.receive<EventBean>()
		println("Received ${event.dataType}")

		if (event is ReviewCreatedFeedEventBean) {
			(service as UpsourceService).processCreateReviewEvent(event)
		}

		call.respondText { "Ok" }
	}


}