package org.belkevglaz.features.upsource

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import org.belkevglaz.config.*
import org.belkevglaz.features.upsource.model.*
import kotlinx.serialization.json.Json as KotlinJson

interface UpsourceClient {
	suspend fun getOpenedReviews(project: Project): List<Review>
}

/**
 * Upsource Rest Api client.
 */
class UpsourceClientImpl(val baseUrl: String) : UpsourceClient {

	companion object {
		const val GET_ALL_REVIEWS = "/~rpc/getReviews"
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
				// todo : get credentials from [AppConfig]
				credentials {
					BasicAuthCredentials(username = "everytag_bot", password = "UzLk!cN23")
				}
			}
		}
	}

	/**
	 * Get all opened review for the [Project].
	 */
	override suspend fun getOpenedReviews(project: Project): List<Review> {
		return client.use<HttpClient, UpsourceResponse> {
			it.post("$baseUrl$GET_ALL_REVIEWS") {
				body = """
				{"projectId":"${project.name}", "query": "state: open", "sortBy": "id,desc", "limit":50}
				""".trimIndent()
			}
		}.result.reviews
	}
}
