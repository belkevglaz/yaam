package org.belkevglaz.features.upsource.model

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */


/**
 * Base Jetbrains Upsource Event webhook beans.
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
	abstract val base: FeedEventBean
}


/**
 * [UserIdBean  ](http://base/~api_doc/integration/Events.html#messages.UserIdBean)
 */
@Serializable
data class UserIdBean(val userId: String? = null, val userName: String? = null, val userEmail: String? = null)

/**
 * @property userId User that initiated the event
 * @property userIds List of users that will receive the event in their feeds
 * @property reviewNumber Associated review number
 * @property reviewId Associated review ID
 * @property date Event date
 * @property actor User that initiated the event
 * @property feedEventId Event ID
 */
@Serializable
data class FeedEventBean(
	val userId: UserIdBean? = null,
	val userIds: Set<UserIdBean>? = null,
	val reviewNumber: Int? = null,
	val reviewId: String? = null,
	val date: Long,
	val actor: UserIdBean,
	val feedEventId: String? = null
)

/**
 * [ParticipantStateChangedFeedEventBean](http://base/~api_doc/integration/Events.html#messages.ParticipantStateChangedFeedEventBean)
 *
 * @property base Base feed event
 * @property participant Participant user info
 * @property oldState Old state
 * @property newState New state
 */
@Serializable
data class ParticipantStateChangedFeedEventBean(
	override val base: FeedEventBean,
	val participant: UserIdBean,
	val oldState: Int,
	val newState: Int,
) : EventBeanData()

/**
 * Bean class for Jetbrains Upsource participant accept or reject review event.
 */
@ExperimentalSerializationApi
@Serializable
@SerialName("ParticipantStateChangedFeedEventBean")
data class ParticipantStateChangedFeedEventBeanCommon(
	override val majorVersion: Int,
	override val minorVersion: Int,
	override val projectId: String,
	override val dataType: String,

	override val data: ParticipantStateChangedFeedEventBean,
) : EventBean()


