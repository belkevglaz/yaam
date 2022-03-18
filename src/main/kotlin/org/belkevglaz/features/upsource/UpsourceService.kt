package org.belkevglaz.features.upsource

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import mu.*
import org.belkevglaz.config.*
import org.belkevglaz.features.teamcity.*
import org.belkevglaz.features.upsource.model.*
import org.koin.core.component.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */

private val logger = KotlinLogging.logger {}

private val json = Json {
	allowStructuredMapKeys = true
}

@ExperimentalSerializationApi
class UpsourceService(appConfig: AppConfig) : KoinComponent {

	private val config = appConfig

	private val client by inject<UpsourceClient>()

	/**
	 *  Retrieve reviews (ready to close or not) from Upsource project.
	 *
	 *  @param projectId Upsource project id
	 *  @param readyToClose flag to get or not reviews that are ready to close
	 */
	suspend fun fetchReviews(projectId: String, readyToClose: Boolean): Set<ReviewDescriptorDTO> {
		val query = if (readyToClose) "state: open and #{ready to close}" else "state: open"
		val sortBy = """""sortBy": "id,desc""""

		return client.getReviews(ReviewsRequestDTO(30, query, sortBy, projectId, null))
	}

	/**
	 * Find related reviews by issue tracker tasks number with given [review].
	 *
	 * Let's assume that the branch part (issue tracker task number) is the same.
	 */
	suspend fun findRelatedReview(review: ReviewDescriptorDTO): List<ReviewDescriptorDTO> {

		if (review.branches().size > 1) {
			logger.warn { "Attention!. Review [${review.reviewId.reviewId}] contains more than 1 branches." }
		}

		val branch = review.branches().first()
		val regex = config.upsource.taskRegexp.toRegex()
		val task = regex.find(branch)?.value
		val openedReviews = fetchReviews(review.reviewId.projectId, false)

		// need to filter related reviews by task
		return if (task != null) {
			logger.info { " 🔎 Search related tasks for branch = [$branch]. Task key = [$task]" }
			openedReviews.filter { r ->
				r.containsBranch { regex.find(it.toString())?.value.equals(task) }
						&& r.reviewId.reviewId != review.reviewId.reviewId
			}
		} else {
			// if task == null, that will find exactly same branch last part.
			val last = branch.split("/").last()
			logger.info { "Task empty. Try to find related reviews with the same part last [$last]" }

			openedReviews.filter { r ->
				r.containsBranch {
					it.toString().split("/").last() == last
				} && r.reviewId.reviewId != review.reviewId.reviewId
			}
		}
	}

	/**
	 * todo :
	 */
	suspend fun buildReadyToCloseReviews(publisherBuildStatus: PublisherBuildStatus): Set<ReviewDescriptorDTO> {

		// find ready to close reviews, related reviews and join revisions.
		val readyToCloseWithRelated = fetchReviews(publisherBuildStatus.project, true)
			.map { review ->
				// join revisions to reviews
				review.apply { revisions = client.getRevisionInReview(review.reviewId) }

			}.also {
				logger.info { "Found [${it.size} reviews that ready to close: ${it.joinToString { r -> r.reviewId.reviewId }}]" }
			}.filter {
				// filter review that has given revision. Use sorted revisions and check most recent (first)
				it.revisions.isNotEmpty() && it.revisions.firstOrNull()?.revisionId == publisherBuildStatus.revision
			}.also {
				logger.info { "Filter reviews with revision [${publisherBuildStatus.revision}]. There are ${it.size} reviews left." }
			}.map {
				// join related reviews with theirs revisions.
				(it to findRelatedReview(it).map { related ->
					related.apply { revisions = client.getRevisionInReview(related.reviewId) }
				}).also {
					logger.info { "Review [${it.first.reviewId.reviewId}] has related [${it.second.joinToString { rr -> rr.reviewId.reviewId }}]" }
				}
			}.map {
				// join revision build statuses for related reviews
				it.apply {
					second.map { related -> joinRevisionsBuilds(related) }
				}
			}.filter {
				// all related reviews are ready to close.
				it.second.all { related -> related.readyToClose() }
			}.flatMap { r ->
				setOf(r.first).union(r.second)
			}.toSet()

		logger.debug { "Completely ready to close reviews: \n" + json.encodeToString(readyToCloseWithRelated) }
		return readyToCloseWithRelated
	}


	/**
	 * todo :
	 */
	suspend fun closeReviews(reviews: Set<ReviewDescriptorDTO>) =
		reviews.forEach {
			client.closeReview(CloseReviewRequestDTO(it.reviewId, true))
				.also { resp ->
					logger.info { "Review [${it.reviewId.reviewId}] closing result: ${resp}" }
				}
		}

	/**
	 *  todo :
	 */
	private suspend fun joinRevisionsBuilds(review: ReviewDescriptorDTO): ReviewDescriptorDTO =
		review.apply {
			// join revisions build statuses to related review
			val buildsRequest = RevisionListDTO(review.reviewId.projectId,
				revisions.map { rev -> rev.revisionId }.toSet())
			buildStatuses = client.getRevisionBuildStatus(buildsRequest)
		}

}


