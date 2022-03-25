package org.belkevglaz.features.upsource

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.*
import mu.*
import org.belkevglaz.config.*
import org.belkevglaz.features.upsource.model.*
import kotlinx.serialization.json.Json as KotlinJson

private val logger = KotlinLogging.logger {}

/**
 * Upsource Rest Api client.
 *
 *
 */
@ExperimentalSerializationApi
open class UpsourceClient(appConfig: AppConfig) {

	private val config = appConfig.upsource

	companion object {
		const val GET_REVIEWS = "/~rpc/getReviews"
		const val GET_REVIEW_DETAILS = "/~rpc/getReviewDetails"
		const val GET_REVISION_LIST = "/~rpc/getRevisionsListFiltered"
		const val GET_REVISION_IN_REVIEW = "/~rpc/getRevisionsInReview"
		const val GET_REVISION_BUILD_STATUS = "/~rpc/getRevisionBuildStatus"
		const val GET_REVISION_BRANCHES = "/~rpc/getRevisionBranches"
		const val GET_REVISION_REVIEW_INFO = "/~rpc/getRevisionReviewInfo"
		const val CLOSE_REVIEW = "/~rpc/closeReview"
		const val FIND_COMMITS = "/~rpc/findCommits"
	}

	private val client = HttpClient(CIO) {
		install(Logging) {
			logger = Logger.DEFAULT
			level = LogLevel.BODY
		}


		install(HttpTimeout) {
			requestTimeoutMillis = 1000 * 60 * 5
			connectTimeoutMillis = 1000 * 60 * 5
			socketTimeoutMillis = 1000 * 60 * 5
		}
		install(JsonFeature) {
			serializer = KotlinxSerializer(KotlinJson {
				isLenient = true
				ignoreUnknownKeys = true
			})
		}

		install(Auth) {
			basic {
				sendWithoutRequest {
					true
				}
				credentials {
					BasicAuthCredentials(username = config.username, password = config.password)
				}
			}
		}

		defaultRequest {
			contentType(ContentType.Application.Json)
			method = HttpMethod.Post
		}
	}

	/**
	 * Template rpc call.
	 *
	 * See all rpc calls: [UpsourceRPC](http://base/~api_doc/reference/Service.html#messages.UpsourceRPC)
	 */
	private suspend inline fun <reified R> getRpc(request: Any, rpcMethod: String): R? =
		try {
			client.request<HttpStatement>(config.url + rpcMethod) {
				body = request
			}.execute {
				return@execute it.receive<R>()
			}
		} catch (e: ClientRequestException) {
			logger.error { e }
			null
		}


	suspend fun getReviewDetails(request: ReviewIdDTO) =
		getRpc<ReviewDescriptorDTOResult>(request, GET_REVIEW_DETAILS)?.result


	/**
	 * Returns the list of reviews
	 */
	suspend fun getReviews(request: ReviewsRequestDTO): Set<ReviewDescriptorDTO> =
		getRpc<ReviewsResponse>(request, GET_REVIEWS)?.result?.reviews ?: emptySet()


	/**
	 * Returns short review information for a set of revisions
	 */
	suspend fun getRevisionReviewInfo(request: RevisionListDTO): RevisionReviewInfoListDTO? =
		getRpc<RevisionReviewInfoListDTOResult>(request, GET_REVISION_REVIEW_INFO)?.result

	/**
	 * Returns the list of branches a revision is part of.
	 */
	suspend fun getRevisionBranches(request: RevisionInProjectDTO): RevisionBranchesResponseDTO? =
		getRpc<RevisionBranchesResponseResultDTO>(request, GET_REVISION_BRANCHES)?.result


	/**
	 * Returns build status for revisions
	 */
	suspend fun getRevisionInReview(request: ReviewIdDTO): Set<RevisionInfoDTO> =
		getRpc<RevisionsInReviewResponseResultDTO>(request, GET_REVISION_IN_REVIEW)
			?.result?.allRevisions?.revision?.sortedByDescending { it.revisionDate }?.toSet()
			?: emptySet()

	/**
	 * Fetch revisions build statuses.
	 */
	suspend fun getRevisionBuildStatus(request: RevisionListDTO): Set<RevisionBuildStatusDTO> =
		getRpc<BuildStatusResponse>(request, GET_REVISION_BUILD_STATUS)?.result?.buildStatus ?: emptySet()

	/**
	 * Finds commit(s) with the given commit hash.
	 */
	suspend fun findCommits(request: FindCommitsRequestDTO): Set<FindCommitsResponseCommitsDTO> =
		getRpc<FindCommitsResponseResult>(request, FIND_COMMITS)?.result?.commits ?: emptySet()

	/**
	 * Closes a review.
	 */
	suspend fun closeReview(request: CloseReviewRequestDTO): CloseReviewResponseDTO? =
		getRpc<CloseReviewResponseDTO>(request, CLOSE_REVIEW)


}
