package org.belkevglaz.features.serialization

import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import org.belkevglaz.features.upsource.model.*

/**
 * Serialization declaration.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */
@ExperimentalSerializationApi
val module = SerializersModule {
	polymorphic(EventBean::class) {
		subclass(ReviewCreatedFeedEventBean::class)
		subclass(ParticipantStateChangedFeedEventBean::class)
	}
}