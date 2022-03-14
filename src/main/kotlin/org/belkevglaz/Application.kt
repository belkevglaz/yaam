package org.belkevglaz

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.html.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.belkevglaz.config.*
import org.belkevglaz.di.*
import org.koin.ktor.ext.*
import org.koin.logger.*
import org.slf4j.*
import org.slf4j.LoggerFactory.*


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun <T : Any> T.logger(): Logger = getLogger(javaClass)

@ExperimentalSerializationApi
fun Application.main() {

	install(ContentNegotiation) {
		json(Json {
			serializersModule = org.belkevglaz.features.serialization.serialize
			ignoreUnknownKeys = true
		})
	}

	install(Locations)

	install(Koin) {
		slf4jLogger()
		modules(appKoinModule)
	}

	// populate config
	setupConfig()

	install(CallLogging)
	routing {
		get("/") {
			call.respondHtml {
				head {
					title { +"Ktor: " }
				}
				body {
					val runtime = Runtime.getRuntime()
					p { +"Hello from Ktor Netty engine running in Docker sample application" }
					p { +"Runtime.getRuntime().availableProcessors(): ${runtime.availableProcessors()}" }
					p { +"Runtime.getRuntime().freeMemory(): ${runtime.freeMemory()}" }
					p { +"Runtime.getRuntime().totalMemory(): ${runtime.totalMemory()}" }
					p { +"Runtime.getRuntime().maxMemory(): ${runtime.maxMemory()}" }
					p { +"System.getProperty(\"user.name\"): ${System.getProperty("user.name")}" }
				}
			}
		}
	}

}
