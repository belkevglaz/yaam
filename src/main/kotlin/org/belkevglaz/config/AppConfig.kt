package org.belkevglaz.config

import io.ktor.application.*
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

	/**
	 *
	 */
	lateinit var projects: List<Project>

	lateinit var teamcity: TeamcityConfig

}

/**
 * Configuration of TeamCity. Pass [url], [username] and [password].
 *
 * @property url        hostname and port
 * @property username   login
 * @property password   password
 */
data class TeamcityConfig(val url: String, val username: String, val password: String)


/**
 * Data config class for Upsource integration.
 *
 */
data class UpsourceConfig(
	val url: String, val username: String, val password: String,
	val branchRegexp: String = "", val taskRegexp: String = "",
)

/**
 *
 */
data class Project(val id: String)


/**
 * [Application] extension that populating config with custom properties.
 */
fun Application.setupConfig() {
	val appConfig by inject<AppConfig>()

	environment.config.config("ktor.teamcity").also {
		appConfig.teamcity = TeamcityConfig(
			it.property("url").getString(),
			it.property("username").getString(),
			it.property("password").getString()
		)
	}

	environment.config.config("ktor.upsource").also {
		appConfig.upsource = UpsourceConfig(
			it.property("url").getString(),
			it.property("username").getString(),
			it.property("password").getString(),
			it.property("branchRegexp").getString(),
			it.property("taskRegExp").getString(),
		)
	}

	appConfig.projects = environment.config.config("ktor.projects").configList("all")
		.map { c ->
			Project(c.property("id").getString())
		}

	println(appConfig.projects)
	println(appConfig.upsource)

}
