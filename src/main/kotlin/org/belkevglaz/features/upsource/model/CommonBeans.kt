package org.belkevglaz.features.upsource.model

import kotlinx.serialization.*
import java.time.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */
@Serializable
data class UpsourceResponse(val result: ReviewList)

@Serializable
data class ReviewList(val reviews: List<Review>? = null, val hasMore: Boolean, val totalCount: Int)

@Serializable
data class Branch(val array: Set<String>)

/**
 * Upsource Review. If this review is a branch review then [branch] array should be filled, else [mergeFromBranch].
 *
 * @property branch branches if it is a branch review.
 * @property mergeFromBranch name of branch when it is a merge review.
 */
@Serializable
data class Review(
	val reviewId: ReviewId, val title: String, val state: Int,
	val branch: Set<String>? = null, val mergeFromBranch: String? = null,
) {

	val commonBranch: String
		get() = mergeFromBranch
			?: if (branch != null && branch.size == 1) {
				branch.first()
			} else {
				throw Exception("Review has more than 1 branches.")
			}

	/**
	 * Check this review for given [predicate].
	 */
	fun containsBranch(predicate: (String?) -> Boolean): Boolean {
		return if (mergeFromBranch != null) {
			predicate(mergeFromBranch)
		} else {
			branch!!.any { predicate(it) }
		}
	}
}

@Serializable
data class ReviewId(val projectId: String, val reviewId: String)

@Serializable
data class Participant(val role: Int, val userId: String)

@Serializable
data class ParticipantAddRequest(val reviewId: ReviewId, val participant: Participant)

/**********************************************************************************/

/**
 * Revisions list request DTO.
 *
 * @property projectId Project ID in Upsource
 * @property reviewId review ID
 */
@Serializable
data class RevisionsInReviewRequestDTO(
	val projectId: String,
	val reviewId: String,
)

/**
 * Revision in review bean.
 */
data class RevisionsInReviewResponseDTO(
	val allRevisions: RevisionDescriptorListDTO,
	val newRevisions: RevisionsSetDTO,
	val branchHint: String? = null,
	val canTrackBranch: String? = null,
)

/**
 * RevisionDescriptorListDTO.
 *
 * @property revision See [RevisionInfoDTO] parameters
 * @property headHash Head revision ID
 * @property query  Search query
 */
data class RevisionDescriptorListDTO(
	val revision: List<RevisionInfoDTO>,
//	val graph: RevisionListGraphDTO? = null,
	val headHash: String? = null,
	val query: String? = null,
)

/**
 * RevisionsSetDTO.
 *
 * @property revision See [RevisionInfoDTO] parameters
 * @property headHash Head revision ID
 * @property query Search query
 */
data class RevisionsSetDTO(
	val revision: List<RevisionInfoDTO>,
	val headHash: String? = null,
	val query: String? = null,
)

/**
 * RevisionInfoDTO.
 *
 * @property projectId  Project ID in Upsource
 * @property revisionId Upsource revision ID (may differ from VCS revision ID in case of a multi-root project)
 * @property revisionDate Revision date (author date in case of Git which differentiates author and committer dates)
 * @property effectiveRevisionDate  Revision date that agrees with graph topology (this means that child revisions
 *      will always have a larger effective date). In case of Git, can be equal to either author or committer date.
 * @property revisionCommitMessage  Commit message of the revision
 * @property vcsRevisionId  The VCS revision ID
 * @property authorId   User ID of the commit's author
 * @property branchHeadLabel    Branch head labels, if any
 */
data class RevisionInfoDTO(
	val projectId: String,
	val revisionId: String,
	val revisionDate: Instant,
	val effectiveRevisionDate: Instant,
	val revisionCommitMessage: String,
//	val state: RevisionStateEnum,
	val vcsRevisionId: String,
	val authorId: String,
	val branchHeadLabel: Set<String>,
)