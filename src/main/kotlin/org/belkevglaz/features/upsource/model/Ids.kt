package org.belkevglaz.features.upsource.model

import kotlinx.serialization.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1\
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
 * [RevisionInProjectDTO](http://base/~api_doc/reference/Ids.html#messages.RevisionInProjectDTO)
 *
 * @property projectId Project ID in Upsource
 * @property revisionId VCS revision ID
 */
@Serializable
data class RevisionInProjectDTO(val projectId: String, val revisionId: String)