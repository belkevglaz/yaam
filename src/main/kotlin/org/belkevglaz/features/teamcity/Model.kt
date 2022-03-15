package org.belkevglaz.features.teamcity

import kotlinx.serialization.*

/**
 * TeamCity Beans.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */

/**
 * Custom (our) hook request from TeamCity.
 *
 * @property buildId        value of [%teamcity.build.id%]
 * @property projectAlias   alias of project in source code review tool (upsource)
 */
@Serializable
data class TeamcityHookRequest(val buildId: Int, val branchName: String, val projectAlias: String)