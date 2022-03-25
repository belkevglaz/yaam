package org.belkevglaz.config

import io.ktor.application.*
import kotlinx.serialization.*
import org.koin.ktor.ext.*

/**
 * Application configuration with custom properties.
 *
 * @author <a href="mailto:belkevglaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */
class AppConfig {

	/**
	 * Instance of Upsource configuration.
	 */
	lateinit var upsource: UpsourceConfig

	lateinit var teamcity: TeamcityConfig

	lateinit var vcs: VcsConfig

	lateinit var projects: List<Project>

}

/**
 * Configuration of TeamCity. Pass [url], [username] and [password].
 *
 * @property url        hostname and port
 * @property username   login
 * @property password   password
 */
data class TeamcityConfig(val url: String, val username: String, val password: String)

data class UpsourceConfig(
	val url: String,
	val username: String,
	val password: String,
	val branchRegexp: String = "",
	val taskRegexp: String = "",
)

data class VcsConfig(val baseUrl: String, val username: String, val password: String)

@Serializable
data class Project(val vcs: ProjectVcs, val review: ProjectReview)

@Serializable
data class ProjectVcs(val workspace: String, val repo: String)

@Serializable
data class ProjectReview(val id: String, val mapping: String? = null)

/**
 * [Application] extension that populating config with custom properties.
 */
fun Application.setupConfig() {
	val appConfig by inject<AppConfig>()

	environment.config.config("ktor.teamcity").also {
		appConfig.teamcity = TeamcityConfig(it.property("url").getString(),
			it.property("username").getString(),
			it.property("password").getString())
	}

	environment.config.config("ktor.upsource").also {
		appConfig.upsource = UpsourceConfig(
			it.property("url").getString(),
			it.property("username").getString(),
			it.property("password").getString(),
			it.property("branchRegexp").getString(),
			it.property("taskRegexp").getString(),
		)
	}


	environment.config.config("ktor.vcs").also {
		appConfig.vcs = VcsConfig(
			it.property("baseUrl").getString(),
			it.property("username").getString(),
			it.property("password").getString()
		)
	}

	appConfig.projects = environment.config.config("ktor.projects").configList("all").map { c ->
		val vcs = c.config("vcs")
		val review = c.config("review")
		Project(
			ProjectVcs(vcs.property("workspace").getString(), vcs.property("repo").getString()),
			ProjectReview(review.property("id").getString(), review.propertyOrNull("mapping")?.getString())
		)
	}

	println(appConfig.projects)
	println(appConfig.upsource)

}
