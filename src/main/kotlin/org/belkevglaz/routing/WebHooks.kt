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
	val bitbucket: BitbucketService by inject()

	val config: AppConfig by inject()

	// webhooks to handle TeamCity Common Build status publisher's events.
	post("/teamcity/publisher/~buildStatus") {
		val request = call.receive<PublisherBuildStatus>()

		val json = Json {
			allowStructuredMapKeys = true
		}

		logger.info { json.encodeToString(request) }

		when (request.state) {
			"failed" -> call.respondText { "Ok" }
			"success" -> {
				val readyToClose = upsource.buildReadyToCloseReviews(request)

				// for all reviews - project the same
				var reviewsAndPulls: Map<ReviewDescriptorDTO, Set<PullRequestShortInfo>>
				config.projects.firstOrNull { it.review.id == readyToClose.first().reviewId.projectId }
					?.let { project ->

						// join pulls to reviews
						reviewsAndPulls = readyToClose.associateWith {
							bitbucket.fetchPullRequestByCommitId(project,
								it.revisions.first().revisionId)
						}

						// check that all reviews has a pull request. If any not - reject all
						if (reviewsAndPulls.any { it.value.isEmpty() }) {
							logger.info {
								"❌ Not all reviews has pull requests :" + reviewsAndPulls.map { (k, v) -> "[${k.reviewId.reviewId}] -> [${v.joinToString { vv -> vv.id }}]" }
									.joinToString { it }
							}
						} else {
							logger.info { "Request and pulls : \n" + json.encodeToString(reviewsAndPulls) }

							// start to merge and close synchronously
							launch {
								reviewsAndPulls.forEach { (review, pulls) ->

									pulls.map { pull ->
										bitbucket.mergePullRequest(project, pull.id).also { response ->
											logger.info { "PullRequest [${pull.id}] merged on ${response?.closedOn}" }
										}
									}
									upsource.closeReviews(setOf(review))

								}
							}
						}
					}

			}
		}
		call.respondText { "Ok" }

	}

	// webhook own custom events from TeamCity.
/*	post("/teamcity/hooks") {

		val request = call.receive<TeamcityHookRequest>()

		if (request.type.equals(TeamCityEventType.BUILD_COMPLETE)) {

			// find related review by branch.
			val reviews = upsource.findRelatedReviews(request.projectAliasId, request.branchName)

			logger.info {
				" 🔎 Found ${reviews.size} reviews for [${request.branchName}] related reviews: "
					.plus(reviews.joinToString { r -> r.reviewId.reviewId })
			}
			// now check review that successfully builded and ready to close.
			upsource.checkReviewsToClose(reviews)

		}

		call.respond(HttpStatusCode.OK)
	}*/
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
	}

/*	post("/upsource/hooks") {
		val event = call.receive<EventBean>()
		logger.info { "Received ${event.dataType}" }

		if (event is ReviewCreatedFeedEventBean) {
			service.processCreateReviewEvent(event)
		}

		call.respondText { "Ok" }
	}*/


}

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

/*		reviews.groupBy(
			keySelector = { appConfig.upsource.taskRegexp.toRegex().find(it.commonBranch)?.value },
			valueTransform = { it.reviewId.reviewId }
		).forEach { (k, v) -> logger.info { "Task [$k] :" + v.joinToString() } }*/

//		logger.info { reviews.groupingBy { appConfig.upsource.taskRegexp.toRegex().find(it.commonBranch)?.value } }
		call.respondText { "Ok" }
	}

}
