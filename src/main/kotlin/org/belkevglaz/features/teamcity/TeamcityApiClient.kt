package org.belkevglaz.features.teamcity

import mu.*
import org.belkevglaz.config.*
import org.jetbrains.teamcity.rest.*

private val logger = KotlinLogging.logger {}

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */
class TeamcityApiClient(appConfig: AppConfig) {

	val client: TeamCityInstance = TeamCityInstanceFactory.httpAuth(
		serverUrl = appConfig.teamcity.url,
		username = appConfig.teamcity.username,
		password = appConfig.teamcity.password
	)

	/**
	 * Teamcity [Build] by [buildId].
	 */
	fun getBuildById(buildId: Int): Branch? {
		val branch = try {

			client.build(BuildId(buildId.toString())).branch

		} catch (e: TeamCityConversationException) {
			logger.error { e }
			return null
		}
		return branch
	}

}


