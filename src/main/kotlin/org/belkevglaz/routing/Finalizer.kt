package org.belkevglaz.routing

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import mu.*
import org.belkevglaz.config.*
import org.belkevglaz.features.upsource.*
import org.belkevglaz.features.upsource.model.*
import org.belkevglaz.features.vcs.*
import org.koin.core.component.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */
private val logger = KotlinLogging.logger {}

@ExperimentalSerializationApi
class Finalizer() : KoinComponent {


	val upsource: UpsourceService by inject()

	val bitbucket: BitbucketService by inject()

	val config: AppConfig by inject()

	private val json = Json { allowStructuredMapKeys = true }

	suspend fun finalizeReviews(reviews: Set<ReviewDescriptorDTO>) {

		logger.info { json.encodeToString(reviews) }

		// join pulls to reviews
		val reviewsAndPulls: Map<ReviewDescriptorDTO, Set<PullRequestShortInfo>> = reviews.associateWith { review ->
			val p: Pair<Project, String> = realizeProject(review)
			bitbucket.fetchPullRequestByCommitId(review.project ?: throw IllegalArgumentException("Project can not be empty"), p.second)
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
			reviewsAndPulls.forEach { (review, pulls) ->

				pulls.map { pull ->
					bitbucket.mergePullRequest(review.project ?: throw IllegalArgumentException("Project can not be empty"), pull.id).also { response ->
						logger.info { "PullRequest [${pull.id}] merged on ${response?.closedOn}" }
					}
				}
				upsource.closeReviews(setOf(review))

			}
		}
	}

	/**
	 * Upsource todo:
	 */
	private fun realizeProject(review: ReviewDescriptorDTO): Pair<Project, String> {
		val revision = review.revisions.first().revisionId

		return if (revision.contains("-")) {
			val (mapping, commitId) = revision.split("-")
			val project =
				config.projects.first { it.review.id == review.reviewId.projectId && mapping == it.review.mapping }
			review.project = project
			project to commitId
		} else {
			val project = config.projects.first { it.review.id == review.reviewId.projectId }
			review.project = project
			project to revision
		}
	}


}



