package org.belkevglaz.features.vcs

import mu.*
import org.belkevglaz.config.*
import org.koin.core.component.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */

private val logger = KotlinLogging.logger {}


class BitbucketService(appConfig: AppConfig) : KoinComponent {

	val config = appConfig

	private val client by inject<BitbucketApiClient>()


	suspend fun fetchPullRequestByCommitId(project: Project, revisionId: String): Set<PullRequestShortInfo> =
		client.pullRequestByCommit(PullRequestByCommitRequest(project.vcs.workspace,
			"micro.workspaces",
			revisionId))?.values ?: emptySet()

	suspend fun mergePullRequest(project: Project, pullId: String): PullRequestMergeResponse? =
		client.mergePullRequest(PullRequestMergeRequest(project.vcs.workspace, project.vcs.repo, pullId))

}