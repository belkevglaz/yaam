package org.belkevglaz.routing

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import mu.*
import org.belkevglaz.config.*
import org.belkevglaz.features.teamcity.*
import org.belkevglaz.features.upsource.*
import org.belkevglaz.features.upsource.model.*
import org.belkevglaz.features.vcs.*
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

//		admin()
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
	val bitbucket: BitbucketService by inject()

	val config: AppConfig by inject()

	route("/teamcity/publisher") {
		// webhooks to handle TeamCity Common Build status publisher's events.
		get("/~rpc/getCurrentUser") {
			call.respondText { "Ok" }
		}

		post("/~buildStatusTestConnection") {
			call.respondText { "Ok" }
		}

		post("/~buildStatus") {
			launch {
				val request = call.receive<PublisherBuildStatus>()

				val json = Json {
					allowStructuredMapKeys = true
				}

				logger.info { json.encodeToString(request) }

				when (request.state) {
					"success" -> {
						val readyToClose = upsource.processBuildStatus(request)

						Finalizer().finalizeReviews(readyToClose)

					}
				}
			}
			call.respondText { "Ok" }

		}
	}
}

/**
 * Webhook handlers from Jetbrains Upsource.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 */
@ExperimentalSerializationApi
fun Routing.upsourceWebhook() {

	val service: UpsourceService by inject()


	get("/upsource/hooks") {
		call.respondText { "Upsource hook get Ok" }
	}

	post("/upsource/hooks") {

		val event = call.receive<EventBean>()
		logger.info { "Received ${event.dataType}" }
		launch {
			when (event) {
				is ParticipantStateChangedFeedEventBeanCommon -> {

					val readyToClose = service.processParticipantStateChange(event.data)

					Finalizer().finalizeReviews(readyToClose)
				}
			}
		}

		call.respondText { "Ok" }
	}


}

/*
@ExperimentalSerializationApi
fun Routing.admin() {

	val appConfig: AppConfig by inject()
	val upsource: UpsourceService by inject()

	get("/admin/recheck") {

		val projectId = "micro-workspaces"
		val reviews = upsource.fetchReviews(projectId, false)

		// before getting build statuses, waits some interval to able upsource
//		reviews.map {
//			it.revisions = upsource.fetchRevisionInReview(it.reviewId)
//			logger.info { "Review [${it.reviewId.reviewId}] has revisions count = ${it.revisions.size ?: 0}" }
//		}

*/
/*		reviews.groupBy(
			keySelector = { appConfig.upsource.taskRegexp.toRegex().find(it.commonBranch)?.value },
			valueTransform = { it.reviewId.reviewId }
		).forEach { (k, v) -> logger.info { "Task [$k] :" + v.joinToString() } }*//*


//		logger.info { reviews.groupingBy { appConfig.upsource.taskRegexp.toRegex().find(it.commonBranch)?.value } }
		call.respondText { "Ok" }
	}

}
*/
