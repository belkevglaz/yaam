package org.belkevglaz.features.upsource.model

import kotlinx.serialization.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */


/**
 * Review id POJO. See a [Upsource API Reference](~api_doc/reference/Ids.html#messages.ReviewIdDTO)
 *
 * @property projectId Project ID in Upsource.
 * @property reviewId Review ID assigned to it when it's created.
 */
@Serializable
data class ReviewIdDTO(val projectId: String, val reviewId: String)

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
data class RevisionBuildStatusDTO(val projectId: String, val revisionId: String, val keys: Set<RevisionBuildStatusKeyDTO>)

/**
 * [RevisionBuildStatusKeyDTO]
 *
 * @property status Build status: Success(1), Failed(2), InProgress(3). See [BuildStatusEnum]
 * @property name Build name (e.g. "#1.0.1000")
 * @property url Build URL (e.g. "http://teamcity-server/build-url")
 * @property description Build description (e.g. "120 of 1500 tests failed")
 */@Serializable
data class RevisionBuildStatusKeyDTO(val status: Int, val name: String? = null, val url: String? = null, val description: String? = null)