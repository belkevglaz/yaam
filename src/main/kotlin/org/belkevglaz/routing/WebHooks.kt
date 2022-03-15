package org.belkevglaz.routing

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.*
import mu.*
import org.belkevglaz.config.*
import org.belkevglaz.features.teamcity.*
import org.belkevglaz.features.upsource.*
import org.belkevglaz.features.upsource.model.*
import org.koin.ktor.ext.*


private val logger = KotlinLogging.logger {}

/**
 * Webhooks module.
 */
@ExperimentalSerializationApi
fun Application.webhooks() {

	routing {
		teamcityWebhook()
		upsourceWebhook()

		admin()
	}
}

/**
 * Webhook handlers from TeamCity.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 */
@ExperimentalSerializationApi
fun Routing.teamcityWebhook() {


	val upsource: UpsourceService by inject()

	post("/teamcity/hooks") {

		val request = call.receive<TeamcityHookRequest>()
		logger.info("Received hook from teamcity. BuildId [${request.buildId}] " +
				"for project [${request.projectAlias}] and branch [${request.branchName}]")

		// find related review by branch.
		val reviews = upsource.findRelatedReviews(request.projectAlias, request.branchName)

		logger.info { "Found for [${request.branchName}] ${reviews.size} reviews: " + reviews.joinToString { r -> r.reviewId.reviewId } }
//		upsource.checkReviews(reviews)

		call.respond(HttpStatusCode.OK)
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
		logger.info { "Received ${event.dataType}" }

		if (event is ReviewCreatedFeedEventBean) {
			service.processCreateReviewEvent(event)
		}

		call.respondText { "Ok" }
	}

	get("/upsource/test") {
		client.getRevisionsListFiltered(null)
		call.respondText { "Ok" }
	}


}

@ExperimentalSerializationApi
fun Routing.admin() {

	val appConfig: AppConfig by inject()
	val upsource: UpsourceService by inject()

	get("/admin/recheck") {

		val reviews = upsource.getOpenedReviews("ild")

		reviews.groupBy(
			keySelector = { appConfig.upsource.taskRegexp.toRegex().find(it.commonBranch)?.value },
			valueTransform = { it.reviewId.reviewId }
		).forEach { (k, v) -> logger.info { "Task [$k] :" + v.joinToString() } }

//		logger.info { reviews.groupingBy { appConfig.upsource.taskRegexp.toRegex().find(it.commonBranch)?.value } }
		call.respondText { "Ok" }
	}

}