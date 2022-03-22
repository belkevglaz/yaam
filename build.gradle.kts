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
	maven {
		url = uri("https://packages.jetbrains.team/maven/p/teamcity-rest-client/teamcity-rest-client")
		name = "JetBrains Repo"
	}
}

dependencies {
	// ktor server
	implementation("io.ktor:ktor-server-core:$ktor_version")
	implementation("io.ktor:ktor-server-netty:$ktor_version")
	implementation("io.ktor:ktor-locations:$ktor_version")

	// ktor client
	implementation("io.ktor:ktor-client-core:$ktor_version")
	implementation("io.ktor:ktor-client-cio:$ktor_version")
	implementation("io.ktor:ktor-client-auth:$ktor_version")
	implementation("io.ktor:ktor-client-serialization:$ktor_version")
	implementation("io.ktor:ktor-client-logging:$ktor_version")
	// support date serialization
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")

	// web
	implementation("io.ktor:ktor-html-builder:$ktor_version")
//	implementation("io.ktor:ktor-jackson:$ktor_version")
	implementation("io.ktor:ktor-serialization:$ktor_version")

	// teamcity support api client
	implementation("org.jetbrains.teamcity:teamcity-rest-client:1.17.1")

//	 bitbucket
//	implementation("org.bitbucket.dpenkin:bitbucket-cloud-rest-client:4.0.3")

	// logging
	implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")


	implementation("ch.qos.logback:logback-classic:$logback_version")

	// koin
	implementation("io.insert-koin:koin-ktor:$koin_version")
	implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
	implementation("io.insert-koin:koin-core:$koin_version")
	testImplementation("io.insert-koin:koin-test:$koin_version")

	testImplementation("io.ktor:ktor-server-tests:$ktor_version")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}