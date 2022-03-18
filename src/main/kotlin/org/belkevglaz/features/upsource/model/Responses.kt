package org.belkevglaz.features.upsource.model

import kotlinx.serialization.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */






/**
 * Revisions in review payload response.
 */
@Serializable
data class RevisionsInReviewResponseResultDTO(val result: RevisionsInReviewResponseDTO)

/**
 * Revision in review bean.
 */
@Serializable
data class RevisionsInReviewResponseDTO(val allRevisions: RevisionDescriptorListDTO, val branchHint: String? = null, val canTrackBranch: String? = null)

/**
 * Revision Build status request.
 */
@Serializable
data class BuildStatusRequest(val projectId: String, val revisionId: Set<String>)


@Serializable
data class BuildStatusResponse(val result: RevisionBuildStatusListDTO?)
