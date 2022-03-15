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
 */
@ExperimentalSerializationApi
class UpsourceClient(appConfig: AppConfig) {

	private val config = appConfig.upsource

	companion object {
		const val GET_ALL_REVIEWS = "/~rpc/getReviews"
		const val ADD_PARTICIPANT = "/~rpc/addParticipantToReview"
		const val GET_REVISION_LIST = "/~rpc/getRevisionsListFiltered"
		const val GET_REVISION_IN_REVIEW = "/~rpc/getRevisionsInReview"
	}

	private val client = HttpClient(CIO) {
		install(Logging) {
			logger = Logger.DEFAULT
			level = LogLevel.ALL
		}

		install(HttpTimeout)
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
	}

	/**
	 * Get all opened review for the [Project] by given projectId [projectId].
	 */
	suspend fun getOpenedReviews(projectId: String): List<Review> {
		return getOpenedReviews(Project(id = projectId))
	}

	/**
	 * Get all opened review for the [Project].
	 */
	suspend fun getOpenedReviews(project: Project): List<Review> =
		try {
			client.request<HttpStatement>("${config.url}$GET_ALL_REVIEWS") {
				method = HttpMethod.Post
				body = """
					{"projectId":"${project.id}", "query": "state: open", "sortBy": "id,desc", "limit":50}
					""".trimIndent()
				timeout {
					requestTimeoutMillis = 1000 * 60 * 5
					connectTimeoutMillis = 1000 * 60 * 5
					socketTimeoutMillis = 1000 * 60 * 5
				}
			}.execute { response ->
				return@execute response.receive<UpsourceResponse>().result.reviews ?: emptyList()
			}
		} catch (e: ClientRequestException) {
			logger.error { e }
			emptyList()
		}


	/**
	 * Add participant (themselves) to review.
	 *
	 * @param reviewId - instance of [ReviewId]
	 * @param participant - instance of [Participant]
	 */
	suspend fun addParticipantToReview(reviewId: ReviewId, participant: Participant) {
		//
		return client.use {
			it.post("${config.url}$ADD_PARTICIPANT") {
				contentType(ContentType.Application.Json)
				body = ParticipantAddRequest(reviewId, participant)
			}
		}
	}

	suspend fun getRevisionInReview(request: RevisionsInReviewRequestDTO): RevisionsInReviewResponseDTO {
		return client.use {
			it.post("${config.url}$GET_REVISION_IN_REVIEW") {
				contentType(ContentType.Application.Json)
				body = request
			}
		}
	}


	suspend fun getRevisionsListFiltered(request: RevisionsInReviewRequestDTO?) {
		val response = client.use<HttpClient, String> {
			it.post("${config.url}$GET_REVISION_LIST") {
				contentType(ContentType.Application.Json)
				body = RevisionsInReviewRequestDTO(
					"micro-workspaces",
					"branch: feature/check-teamcity-pr-2",
				)
			}
		}

		println { response }
	}
}
