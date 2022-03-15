package org.belkevglaz.features.upsource

import kotlinx.serialization.*
import mu.*
import org.belkevglaz.config.*
import org.belkevglaz.features.upsource.model.*
import org.koin.core.component.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */

private val logger = KotlinLogging.logger {}

@ExperimentalSerializationApi
class UpsourceService(appConfig: AppConfig) : KoinComponent {

	private val config = appConfig

	private val client by inject<UpsourceClient>()

	/**
	 * Processing review creation event.
	 */
	suspend fun processCreateReviewEvent(event: ReviewCreatedFeedEventBean) {
		logger.debug("Received $event")

		// check need to add a bot
		if (config.projects.first { it.id == event.projectId }.nobot != true) {
			client.addParticipantToReview(
				ReviewId(event.projectId, event.data.base.reviewId),
				Participant(2, "null"))
		}
	}

	/**
	 * Find related reviews by issue tracker tasks number with given [projectId] and [branchName].
	 *
	 * Let's assume that the branch part (issue tracker task number) is the same.
	 */
	suspend fun findRelatedReviews(projectId: String, branchName: String): List<Review> {

		// todo : move regexp into configuration
		val regex = """"${config.upsource.taskRegexp}""".toRegex()

		val task = regex.find(branchName)?.value

		val opened = client.getOpenedReviews(projectId)

		return if (task != null) {
			logger.info { "Will try to filter opened review with task [$task], that was got from branch [$branchName]" }

			opened.filter { r -> r.containsBranch { regex.find(it.toString())?.value.equals(task) } }
		} else {
			// if task == null, that will find exactly same branch last part.
			val last = branchName.split("/").last()
			logger.info { "Try to find reviews with the same part last [$last]" }

			opened.filter { r -> r.containsBranch { it.toString().split("/").last() == last } }
		}

	}

	suspend fun getOpenedReviews(projectId: String): List<Review> {
		return client.getOpenedReviews(projectId)
	}

}

