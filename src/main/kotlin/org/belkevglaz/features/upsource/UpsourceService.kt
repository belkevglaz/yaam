package org.belkevglaz.features.upsource

import kotlinx.serialization.*
import org.belkevglaz.*
import org.belkevglaz.config.*
import org.belkevglaz.features.upsource.model.*
import org.koin.core.component.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */

@ExperimentalSerializationApi
class UpsourceService(appConfig: AppConfig) : KoinComponent {

	private val config = appConfig

	private val client by inject<UpsourceClient>()

	/**
	 * Processing review creation event.
	 */
	suspend fun processCreateReviewEvent(event: ReviewCreatedFeedEventBean) {
		logger().debug("Received $event")

		// check need to add a bot
		if (config.projects.first { it.name == event.projectId }.nobot != true) {
			client.addParticipantThemselvesToReview(event)
		}
	}
}

