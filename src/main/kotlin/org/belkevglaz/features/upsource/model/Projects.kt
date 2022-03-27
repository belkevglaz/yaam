package org.belkevglaz.features.upsource.model

import kotlinx.serialization.*
import org.belkevglaz.config.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */

/**
 * RevisionInfoDTO.
 *
 * @property projectId  Project ID in Upsource
 * @property revisionId Upsource revision ID (may differ from VCS revision ID in case of a multi-root project)
 * @property revisionDate Revision date (author date in case of Git which differentiates author and committer dates)
 * @property revisionCommitMessage  Commit message of the revision
 * @property authorId   User ID of the commit's author
 * @property branchHeadLabel    Branch head labels, if any
 */
@Serializable
data class RevisionInfoDTO(
	val projectId: String,
	val revisionId: String,
	val revisionDate: Long,
	val revisionCommitMessage: String,
	val authorId: String,
	val branchHeadLabel: Set<String>? = null,
)

/**
 * RevisionDescriptorListDTO.
 *
 * @property revision See [RevisionInfoDTO] parameters
 * @property headHash Head revision ID
 * @property query  Search query
 */
@Serializable
data class RevisionDescriptorListDTO(
	val revision: Set<RevisionInfoDTO>? = null,
	val headHash: String? = null,
	val query: String? = null,
)


/**
 * See [RevisionListDTO](http://base/~api_doc/reference/Projects.html#messages.RevisionListDTO)
 *
 * @property projectId Project ID in Upsource
 * @property revisionId IDs of the requested revisions
 */
@Serializable
data class RevisionListDTO(val projectId: String, val revisionId: Set<String>)

@Serializable
data class RevisionReviewInfoListDTOResult(val result: RevisionReviewInfoListDTO)

/**
 * See [RevisionReviewInfoListDTO](http://base/~api_doc/reference/Projects.html#messages.RevisionReviewInfoListDTO)
 *
 * @property reviewInfo See [RevisionReviewInfoDTO] parameters
 */
@Serializable
data class RevisionReviewInfoListDTO(val reviewInfo: Set<RevisionReviewInfoDTO>? = null)

/**
 * See [RevisionReviewInfoDTO](http://base/~api_doc/reference/Projects.html#messages.RevisionReviewInfoDTO)
 *
 * @property reviewInfo See [ShortReviewInfoDTO] parameters
 */
@Serializable
data class RevisionReviewInfoDTO(val reviewInfo: ShortReviewInfoDTO? = null)

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
 * Revisions in review payload response.
 */
@Serializable
data class RevisionsInReviewResponseResultDTO(val result: RevisionsInReviewResponseDTO)

/**
 * Revision in review bean.
 */
@Serializable
data class RevisionsInReviewResponseDTO(
	val allRevisions: RevisionDescriptorListDTO,
	val branchHint: String? = null,
	val canTrackBranch: String? = null,
)

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
	@Required
	val limit: Int = 30,
	val query: String? = null,
	@Required
	val sortBy: String = "id,desc",
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

@Serializable
data class ReviewDescriptorDTOResult(val result: ReviewDescriptorDTO)

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

	var project: Project? = null

}

/**
 * Review participant. See a [Upsource API Reference](~api_doc/reference/Projects.html#messages.ParticipantInReviewDTO)
 *
 * @property userId Participant user ID
 * @property role Participant's role in the review. See RoleInReviewEnum parameters
 * @property state of the participant. See ParticipantStateEnum parameters
 */
@Serializable
data class ParticipantInReviewDTO(val userId: String, val role: Int, val state: Int? = null)

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


/**
 * [FindCommitsRequestDTO](http://base/~api_doc/reference/Projects.html#messages.FindCommitsRequestDTO)
 *
 * @property commits See [FindCommitsRequestPatternDTO] parameters
 * @property requestChanges Whether revision changes should be returned along with revision metadata (unused, left for compatibility with older clients)
 * @property limit Number of commits to return
 */
@Serializable
data class FindCommitsRequestDTO(
	val commits: FindCommitsRequestPatternDTO,
	val requestChanges: Boolean? = null,
	val limit: Int? = null,
)

/**
 * [](http://base/~api_doc/reference/Projects.html#messages.FindCommitsRequestPatternDTO)
 *
 * @property revisionId VCS revision ID
 * @property projectId Project ID in Upsource
 * @property messageFragment A fragment of the commit message used as a search query
 * @property author Name of the commit author
 * @property commitTime Unix timestamp of the commit
 */
@Serializable
data class FindCommitsRequestPatternDTO(
	@Required val revisionId: String? = null,
	val projectId: String? = null,
	val messageFragment: String? = null,
	val author: String? = null,
	val commitTime: Long? = null,
)

@Serializable
data class FindCommitsResponseResult(val result: FindCommitsResponseDTO)

/**
 * [FindCommitsResponseDTO](http://base/~api_doc/reference/Projects.html#messages.FindCommitsResponseDTO)
 *
 * @property commits See [FindCommitsResponseCommitsDTO] parameters
 */
@Serializable
data class FindCommitsResponseDTO(val commits: Set<FindCommitsResponseCommitsDTO>? = null)


/**
 * [FindCommitsResponseCommitsDTO](http://base/~api_doc/reference/Projects.html#messages.FindCommitsResponseCommitsDTO)
 *
 * @property commits See [FindCommitsResponseCommitDTO] parameters
 */
@Serializable
data class FindCommitsResponseCommitsDTO(val commits: Set<FindCommitsResponseCommitDTO>? = null)

/**
 * [FindCommitsResponseCommitDTO](http://base/~api_doc/reference/Projects.html#messages.FindCommitsResponseCommitDTO)
 *
 * @property projectId Project ID in Upsource
 * @property projectName Project name
 * @property revision See [RevisionInfoDTO] parameters
 * @property changes See RevisionsDiffDTO parameters
 */
@Serializable
data class FindCommitsResponseCommitDTO(
	val projectId: String,
	val projectName: String,
	val revision: RevisionInfoDTO,
	@Transient val changes: String = "",
)

/**
 * Revision Build status request.
 */
@Serializable
data class BuildStatusRequest(val projectId: String, val revisionId: Set<String>)


@Serializable
data class BuildStatusResponse(val result: RevisionBuildStatusListDTO?)

/**
 * [RevisionBuildStatusListDTO]
 *
 * @property buildStatus container for build status.
 */
@Serializable
data class RevisionBuildStatusListDTO(val buildStatus: Set<RevisionBuildStatusDTO>? = null)

/**
 * [RevisionBuildStatusDTO]
 *
 * @property projectId Project ID in Upsource
 * @property revisionId VCS revision ID
 * @property keys A unique build identifier (e.g. PROJECT-VERSION-1234)
 */
@Serializable
data class RevisionBuildStatusDTO(
	val projectId: String,
	val revisionId: String,
	val keys: Set<RevisionBuildStatusKeyDTO>,
)

/**
 * [RevisionBuildStatusKeyDTO]
 *
 * @property status Build status: Success(1), Failed(2), InProgress(3). See [BuildStatusEnum]
 * @property name Build name (e.g. "#1.0.1000")
 * @property url Build URL (e.g. "http://teamcity-server/build-url")
 * @property description Build description (e.g. "120 of 1500 tests failed")
 */
@Serializable
data class RevisionBuildStatusKeyDTO(
	val status: Int,
	val name: String? = null,
	val url: String? = null,
	val description: String? = null,
)