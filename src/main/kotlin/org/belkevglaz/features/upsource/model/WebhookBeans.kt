package org.belkevglaz.features.upsource.model

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*


@ExperimentalSerializationApi
val module = SerializersModule {
	polymorphic(EventBean::class) {
		subclass(ReviewCreatedFeedEventBean::class)
		subclass(ParticipantStateChangedFeedEventBean::class)
	}
}

/**
 * Base Jetbrains Upsource Event bean.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */
@ExperimentalSerializationApi
@JsonClassDiscriminator("dataType")
@Serializable
abstract class EventBean {
	abstract val majorVersion: Int
	abstract val minorVersion: Int
	abstract val projectId: String

	abstract val dataType: String

	abstract val data: EventBeanData
}

/**
 * Abstract class for common data block within upsource events.
 */
@Serializable
abstract class EventBeanData {
	abstract val base: Base
}


/**
 * Bean class for Jetbrains Upsource create review event.
 */
@ExperimentalSerializationApi
@Serializable
@SerialName("ReviewCreatedFeedEventBean")
data class ReviewCreatedFeedEventBean(
	override val majorVersion: Int,
	override val minorVersion: Int,
	override val projectId: String,
	override val dataType: String,

	override val data: ReviewCreatedFeedEventBeanData,
) : EventBean()

/**
 * Bean class for Jetbrains Upsource participant accept or reject review event.
 */
@ExperimentalSerializationApi
@Serializable
@SerialName("ParticipantStateChangedFeedEventBean")
data class ParticipantStateChangedFeedEventBean(
	override val majorVersion: Int,
	override val minorVersion: Int,
	override val projectId: String,
	override val dataType: String,

	override val data: ParticipantStateChangedFeedEventBeanData,
) : EventBean()

/**
 * Pojo base tag common class.
 */
@Serializable
data class Base(
	val userIds: List<User>,
	val reviewNumber: Int,
	val reviewId: String,
	val date: Long,
	val actor: User,
)

/**
 * Pojo user represent class.
 */
@Serializable
data class User(val userId: String, val userName: String, val userEmail: String)

/**
 * Data Payload of [ReviewCreatedFeedEventBean].
 */
@Serializable
data class ReviewCreatedFeedEventBeanData(
	override val base: Base,
	val revisions: List<String>,
	val branch: String,
) : EventBeanData()


/**
 * Data Payload of [ParticipantStateChangedFeedEventBean].
 */
@Serializable
data class ParticipantStateChangedFeedEventBeanData(
	override val base: Base,
	val participant: User,
	val oldState: Int,
	val newState: Int,
) : EventBeanData()




