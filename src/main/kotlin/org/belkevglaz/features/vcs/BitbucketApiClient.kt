package org.belkevglaz.features.vcs

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
import mu.*
import org.belkevglaz.config.*

/**
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */

private val logger = KotlinLogging.logger {}

class BitbucketApiClient(appConfig: AppConfig) {

	private val config = appConfig

	/**
	 *	TODO : duplicate with [UpsourceClient]
	 */
	private val client = HttpClient(CIO) {

		install(Logging) {
			logger = Logger.DEFAULT
			level = LogLevel.NONE
		}


		install(HttpTimeout) {
			requestTimeoutMillis = 1000 * 60 * 5
			connectTimeoutMillis = 1000 * 60 * 5
			socketTimeoutMillis = 1000 * 60 * 5
		}
		install(JsonFeature) {
			serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
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
					BasicAuthCredentials(username = config.vcs.username, password = config.vcs.password)
				}
			}
		}

		defaultRequest {
			contentType(ContentType.Application.Json)

		}
	}

	/**
	 * Template rpc call.
	 *
	 * See all rpc calls: [UpsourceRPC](http://base/~api_doc/reference/Service.html#messages.UpsourceRPC)
	 */
	private suspend inline fun <reified R> rest(rest: Any, m: HttpMethod): R? =
		try {
			client.request<HttpStatement>(config.vcs.baseUrl.plus(rest)) {
				method = m
			}.execute {
				return@execute it.receive<R>()
			}
		} catch (e: ClientRequestException) {
			logger.error { e }
			null
		}


	private suspend inline fun <reified R> get(request: Any): R? = rest(request, HttpMethod.Get)

	private suspend inline fun <reified R> post(request: Any): R? = rest(request, HttpMethod.Post)

	/**
	 * Returns a paginated list of all pull requests as part of which this commit was reviewed.
	 * Pull Request Commit Links app must be installed first before using this API;
	 * installation automatically occurs when 'Go to pull request' is clicked from the web interface for a commit's details.
	 *
	 * See [pullRequestByCommit](https://developer.atlassian.com/cloud/bitbucket/rest/api-group-pullrequests/#api-repositories-workspace-repo-slug-commit-commit-pullrequests-get)
	 */
	suspend fun pullRequestByCommit(request: PullRequestByCommitRequest): PullRequestByCommitResponse? =
		get<PullRequestByCommitResponse>("/2.0/repositories/${request.workspace}/${request.repo}/commit/${request.commitId}/pullrequests")


	/**
	 * Merges the pull request.
	 *
	 * See [mergePullRequest](https://developer.atlassian.com/cloud/bitbucket/rest/api-group-pullrequests/#api-repositories-workspace-repo-slug-pullrequests-pull-request-id-merge-post)
	 */
	suspend fun mergePullRequest(request: PullRequestMergeRequest): PullRequestMergeResponse? =
		post<PullRequestMergeResponse>("/2.0/repositories/${request.workspace}/${request.repo}/pullrequests/${request.pullRequestId}/merge")
}