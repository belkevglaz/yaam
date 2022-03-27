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

@Suppress("MemberVisibilityCanBePrivate")
@ExperimentalSerializationApi
class UpsourceService(appConfig: AppConfig) : KoinComponent {

	private val config = appConfig

	private val client by inject<UpsourceClient>()


	/**
	 * Retrieve review by review Id.
	 *
	 * @param reviewId Review Id
	 * @param readyToClose flag to get or not reviews that are ready to close
	 */
	suspend fun fetchReviewById(reviewId: String, readyToClose: Boolean = true): ReviewDescriptorDTO? {
		val query =
			(if (readyToClose) "state: open and #{ready to close}" else "state: open").plus(" and id: $reviewId")
		return client.getReviews(ReviewsRequestDTO(query = query)).firstOrNull()
	}

	/**
	 *  Retrieve reviews (ready to close or not) from Upsource project.
	 *
	 *  @param projectId Upsource project id
	 *  @param readyToClose flag to get or not reviews that are ready to close
	 */
	suspend fun fetchReviews(projectId: String, readyToClose: Boolean): Set<ReviewDescriptorDTO> {
		val query = if (readyToClose) "state: open and #{ready to close}" else "state: open"

		return client.getReviews(ReviewsRequestDTO(query = query, projectId = projectId)).also {
			logger.info { "Found [${it.size}] reviews that ready [$readyToClose] to close: ${it.joinToString { r -> r.reviewId.reviewId }}" }
		}
	}

	/**
	 * Retrieve review from project with [projectId] and that has given commitId [commitId].
	 */
	suspend fun fetchReviewsByCommitId(
		projectId: String,
		commitId: String,
		readyToClose: Boolean = true,
	): Set<ReviewDescriptorDTO> {
		// first we need to retrieve revision relates with this commit
		val commits = client.findCommits(FindCommitsRequestDTO(FindCommitsRequestPatternDTO(commitId, projectId)))

		val revisionIds = commits.flatMap {
			it.commits?.map { c -> c.revision.revisionId }?.toSet() ?: emptySet()
		}

		return client.getRevisionReviewInfo(RevisionListDTO(projectId, revisionIds.toSet()))?.reviewInfo
			?.filter { it.reviewInfo != null }
			?.mapNotNull {
				client.getReviewDetails(it.reviewInfo?.reviewId
					?: throw IllegalArgumentException("ReviewInfo [${it}] has empty reviewInfo"))
			}?.toSet()
			.also {
				logger.info { "Found [${it?.size}] reviews that ready [$readyToClose] to close: ${it?.joinToString { r -> r.reviewId.reviewId }}] by revision [${commitId}]" }
			} ?: emptySet()

	}

	/**
	 * Find related reviews by issue tracker tasks number with given [review].
	 *
	 * Let's assume that the branch part (issue tracker task number) is the same.
	 */
	private suspend fun relatedReviews(review: ReviewDescriptorDTO): List<ReviewDescriptorDTO> {

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
	 * Flat set of given [reviews] themselves and related.
	 */
	private suspend fun relatedReviews(reviews: Set<ReviewDescriptorDTO>): Set<ReviewDescriptorDTO> =
		reviews.map { it to relatedReviews(it) }.flatMap { p -> setOf(p.first).union(p.second) }.toSet()

	/**
	 * Join revisions to given [review].
	 */
	private suspend fun joinRevisionToReview(review: ReviewDescriptorDTO): ReviewDescriptorDTO =
		review.apply { revisions = client.getRevisionInReview(review.reviewId) }

	/**
	 * Join revisions to set of [reviews].
	 */
	suspend fun joinRevisionsToReviews(reviews: Set<ReviewDescriptorDTO>): Set<ReviewDescriptorDTO> =
		reviews.map { joinRevisionToReview(it) }.toSet()

	/**
	 *  Join build states to revisions in given [review].
	 */
	private suspend fun joinRevisionsBuilds(review: ReviewDescriptorDTO): ReviewDescriptorDTO =
		review.apply {
			// convert revisions into set of
			val request = RevisionListDTO(review.reviewId.projectId, revisions.map { rev -> rev.revisionId }.toSet())
			buildStatuses = client.getRevisionBuildStatus(request)
		}


	/**
	 * todo :
	 */
	suspend fun processBuildStatus(status: PublisherBuildStatus): Set<ReviewDescriptorDTO> {

		val reviewsByCommit = fetchReviewsByCommitId(status.project, status.revision)

		return readyToCloseReviews(reviewsByCommit)
	}

	/**
	 * Get related review and related.
	 * Return set of review only if all reviews are read to close.
	 */
	private suspend fun readyToCloseReviews(reviews: Set<ReviewDescriptorDTO>): Set<ReviewDescriptorDTO> {
		val readyToClose = relatedReviews(reviews)
			.map { joinRevisionToReview(it) }
			.map { joinRevisionsBuilds(it) }.toSet()

		return if (readyToClose.all { it.readyToClose() }) readyToClose.also {
			logger.debug { "Completely ready to close reviews: \n" + json.encodeToString(it) }
		} else {
			emptySet()
		}
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
	 * Processing participant state change to accept.
	 */
	suspend fun processParticipantStateChange(event: ParticipantStateChangedFeedEventBean): Set<ReviewDescriptorDTO> {

		if (event.newState != 2) return emptySet()

		val review = fetchReviewById(event.base.reviewId ?: "") ?: return emptySet()

		return readyToCloseReviews(setOf(review))


	}
}


