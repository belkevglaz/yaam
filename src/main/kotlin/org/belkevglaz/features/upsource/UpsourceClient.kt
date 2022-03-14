package org.belkevglaz.features.upsource

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import kotlinx.serialization.*
import org.belkevglaz.config.*
import org.belkevglaz.features.upsource.model.*
import kotlinx.serialization.json.Json as KotlinJson

/**
 * Upsource Rest Api client.
 */
@ExperimentalSerializationApi
class UpsourceClient(appConfig: AppConfig) {

	private val config = appConfig.upsource

	companion object {
		const val GET_ALL_REVIEWS = "/~rpc/getReviews"
		const val ADD_PARTICIPANT = "/~rpc/addParticipantToReview"
	}

	private val client = HttpClient(CIO) {
		install(Logging) {
			logger = Logger.DEFAULT
			level = LogLevel.ALL
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
	}

	/**
	 * Get all opened review for the [Project].
	 */
	suspend fun getOpenedReviews(project: Project): List<Review> {
		return client.use<HttpClient, UpsourceResponse> {
			it.post("${config.url}$GET_ALL_REVIEWS") {
				body = """
				{"projectId":"${project.name}", "query": "state: open", "sortBy": "id,desc", "limit":50}
				""".trimIndent()
			}
		}.result.reviews
	}

	/**
	 * Add participant (themselves) to review.
	 * @param event [ReviewCreatedFeedEventBean]
	 */
	suspend fun addParticipantThemselvesToReview(event: EventBean) {
		//
		return client.use {
			it.post("${config.url}$ADD_PARTICIPANT") {
				body = """
					{
						"reviewId": { "projectId": "${event.projectId}", "reviewId": "${event.data.base.reviewId}" },
						"participant": { "role": 2, "userId": "${config.botId}" }
					}
				""".trimIndent()
			}
		}
	}
}
