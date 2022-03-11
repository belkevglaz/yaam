package org.belkevglaz.features.upsource.model

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */
@kotlinx.serialization.Serializable
data class UpsourceResponse(val result: ReviewList)

@kotlinx.serialization.Serializable
data class ReviewList(val reviews: List<Review>, val hasMore: Boolean, val totalCount: Int)

@kotlinx.serialization.Serializable
data class Review(val reviewId: ReviewId)

@kotlinx.serialization.Serializable
data class ReviewId(val projectId: String, val reviewId: String)