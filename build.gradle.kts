val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String by project

plugins {
	application
	kotlin("jvm") version "1.6.10"
	kotlin( "plugin.serialization" ) version "1.6.10"
}

group = "org.belkevglaz"
version = "0.0.1"
application {
	mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
	mavenCentral()
}

dependencies {
	// ktor core deps
	implementation("io.ktor:ktor-server-core:$ktor_version")
	implementation("io.ktor:ktor-server-netty:$ktor_version")
	implementation("io.ktor:ktor-locations:$ktor_version")

	// web
	implementation("io.ktor:ktor-html-builder:$ktor_version")
//	implementation("io.ktor:ktor-jackson:$ktor_version")
	implementation("io.ktor:ktor-serialization:$ktor_version")

	// mongodb
//	implementation("org.litote.kmongo:kmongo-coroutine:4.5.0")


	implementation("ch.qos.logback:logback-classic:$logback_version")

	// koin
	implementation("io.insert-koin:koin-ktor:$koin_version")
	implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
	implementation("io.insert-koin:koin-core:$koin_version")
	testImplementation("io.insert-koin:koin-test:$koin_version")

	testImplementation("io.ktor:ktor-server-tests:$ktor_version")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}