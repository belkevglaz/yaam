package org.belkevglaz.features.vcs

import kotlinx.serialization.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */
@Serializable
data class PullRequestByCommitRequest(val workspace: String, val repo: String, val commitId: String)

@Serializable
data class PullRequestByCommitResponse(val type: String, val values: Set<PullRequestShortInfo>)

@Serializable
data class PullRequestShortInfo(val type: String, val id: String, val title: String)

@Serializable
data class PullRequestMergeRequest(val workspace: String, val repo: String, val pullRequestId: String)

@Serializable
data class PullRequestMergeResponse(@SerialName("closed_on") val closedOn: String, val title: String)

