package org.belkevglaz.routing;

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

public class WebHooksKtTest {

	@Test
	fun testPostTeamcityComplete() {
		withTestApplication({ webhooks() }) {
			handleRequest(HttpMethod.Post, "/teamcity/complete/*").apply {
//				TODO("Please write your test here")
			}
		}
	}
}