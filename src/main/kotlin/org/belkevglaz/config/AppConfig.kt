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

}


/**
 * Data config class for Upsource integration.
 */
data class UpsourceConfig(val url: String, val username: String, val password: String)

data class Project(val name: String, val alias: String?, val nobot: Boolean? = false)

/**
 * [Application] extension that populating config with custom properties.
 */
fun Application.setupConfig() {
	val appConfig by inject<AppConfig>()

	environment.config.config("ktor.upsource").also {
		appConfig.upsource = UpsourceConfig(
			it.property("url").getString(),
			it.property("username").getString(),
			it.property("password").getString()
		)
	}

	appConfig.projects = environment.config.config("ktor.projects").configList("all")
		.map { c ->
			Project(
				c.property("name").getString(),
				c.propertyOrNull("alias")?.getString(),
				c.propertyOrNull("nobot")?.getString().toBoolean())
		}

	println(appConfig.projects)
	println(appConfig.upsource)

}
