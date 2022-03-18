package org.belkevglaz.features.upsource.model

import kotlinx.serialization.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */


/**
 * See [RevisionListDTO](http://base/~api_doc/reference/Projects.html#messages.RevisionListDTO)
 *
 * @property projectId Project ID in Upsource
 * @property revisionId IDs of the requested revisions
 */
@Serializable
data class RevisionListDTO(val projectId: String, val revisionId: Set<String>)

/**
 * See [RevisionReviewInfoListDTO](http://base/~api_doc/reference/Projects.html#messages.RevisionReviewInfoListDTO)
 *
 * @property reviewInfo See [RevisionReviewInfoDTO] parameters
 */
@Serializable
data class RevisionReviewInfoListDTO(val reviewInfo: Set<RevisionReviewInfoDTO>)

/**
 * See [RevisionReviewInfoDTO](http://base/~api_doc/reference/Projects.html#messages.RevisionReviewInfoDTO)
 *
 * @property reviewInfo See [ShortReviewInfoDTO] parameters
 */
@Serializable
data class RevisionReviewInfoDTO(val reviewInfo: ShortReviewInfoDTO)

/**
 * Response result.
 */
@Serializable
data class RevisionBranchesResponseResultDTO(val result: RevisionBranchesResponseDTO)

/**
 * [RevisionBranchesResponseDTO](http://base/~api_doc/reference/Projects.html#messages.RevisionBranchesResponseDTO)
 *
 * @property branchName Branches containing the given revision
 */
@Serializable
data class RevisionBranchesResponseDTO(val branchName: Set<String>)

/**
 * See [ShortReviewInfoDTO](http://base/~api_doc/reference/Projects.html#messages.ShortReviewInfoDTO)
 *
 * @property reviewId See [ReviewIdDTO] parameters
 * @property title Review title
 * @property state Review state: open(1), closed(2)
 * @property branch Names of tracked branches
 */
@Serializable
data class ShortReviewInfoDTO(val reviewId: ReviewIdDTO, val title: String, val state: Int, val branch: Set<String>)


/**
 * Response for [ReviewsRequestDTO].
 */
@Serializable
data class ReviewsResponse(val result: ReviewListDTO)

/**
 * [ReviewsRequestDTO](http://base/~api_doc/reference/Projects.html#messages.ReviewsRequestDTO).
 *
 * @property limit Number of reviews to return
 * @property query Search query (e.g. "state: open") and/or phrase appearing in review title or discussion
 * @property sortBy Sort by: last updated ("updated", default), review ID ("id,asc", "id,desc"), title ("title"), due date ("deadline,asc", "deadline,desc")
 * @property projectId Project ID in Upsource
 * @property skip Number of reviews to skip from the top (for pagination)
 */
@Serializable
data class ReviewsRequestDTO(
	val limit: Int,
	val query: String? = null,
	val sortBy: String? = null,
	val projectId: String? = null,
	val skip: Int? = null,
)

/**
 * [ReviewListDTO](http://base/~api_doc/reference/Projects.html#messages.ReviewListDTO)
 *
 * @property reviews see [ReviewDescriptorDTO] parameters
 * @property hasMore Whether all available items have been returned or more can be requested by passing the corresponding 'limit' value in the subsequent request
 * @property totalCount Total number of reviews
 */
@Serializable
data class ReviewListDTO(val reviews: Set<ReviewDescriptorDTO>? = null, val hasMore: Boolean, val totalCount: Int)

/**
 * Upsource Review.
 * See a [ReviewDescriptorDTO](~api_doc/reference/Projects.html#messages.RevisionDescriptorListDTO)
 *
 * @property reviewId See [ReviewIdDTO] parameters
 * @property title Review title
 * @property participants See [ParticipantInReviewDTO] parameters
 * @property state Review state: open(1), closed(2)
 * @property isReadyToClose 'true' if all reviewers have accepted the changes but the review is still open
 * @property branch Names of tracked branches
 * @property mergeFromBranch Merge review: branch to merge from
 */
@Serializable
data class ReviewDescriptorDTO(
	val reviewId: ReviewIdDTO,
	val title: String,
	val participants: Set<ParticipantInReviewDTO>? = null,
	val state: Int,
	val isReadyToClose: Boolean? = null,
	val branch: Set<String>? = null,
	val mergeFromBranch: String? = null,
) {

	var revisions: Set<RevisionInfoDTO> = emptySet()

	var buildStatuses: Set<RevisionBuildStatusDTO> = emptySet()

}

/**
 * [CloseReviewRequestDTO](http://base/~api_doc/reference/Projects.html#messages.CloseReviewRequestDTO)
 *
 * @property reviewId See [ReviewIdDTO] parameters
 * @property isFlagged Pass 'true' to close a review, 'false' to reopen a closed review
 */
@Serializable
data class CloseReviewRequestDTO(val reviewId: ReviewIdDTO, val isFlagged: Boolean)

/**
 * [CloseReviewResponseDTO](http://base/~api_doc/reference/Projects.html#messages.CloseReviewResponseDTO)
 *
 * @property syncResult GitHub sync result. See SyncResultEnum parameters
 */
@Serializable
data class CloseReviewResponseDTO(val syncResult: Int? = null)

