package org.belkevglaz.features.teamcity

import kotlinx.serialization.*
import mu.*

/**
 * TeamCity Beans.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */

private val logger = KotlinLogging.logger {}

/**
 * TeamCity event type.
 */
enum class TeamCityEventType {
	BUILD_COMPLETE
}

/**
 * Custom (our) hook request from TeamCity.
 *
 * @property buildId        value of [%teamcity.build.id%]
 * @property projectAliasId   alias of project in source code review tool (upsource)
 */
@Serializable
data class TeamcityHookRequest(
	val buildId: Int,
	val branchName: String,
	val projectAliasId: String,
	val type: TeamCityEventType) {
	init {
		logger.info { "🏗 [$javaClass] created. $this" }
	}
}

/**
 * Pojo class to describe TeamCity Common Status Publisher payload about build status.
 *
 * @property revisionDate revision date in milliseconds
 * @property name teamcity build name
 * @property revisionMessage revision (vcs commit) message
 * @property project project name
 * @property
 * @property
 */
@Serializable
data class PublisherBuildStatus(
	val revisionDate: Long,
	val name: String,
	val revisionMessage: String,
	val project: String,
	val description: String,
	val state: String,
	val url: String,
	val revision: String
)