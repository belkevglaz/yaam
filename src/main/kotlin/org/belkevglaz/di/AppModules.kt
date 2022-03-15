package org.belkevglaz.di

import kotlinx.serialization.*
import org.belkevglaz.config.*
import org.belkevglaz.features.teamcity.*
import org.belkevglaz.features.upsource.*
import org.koin.dsl.*

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */
@ExperimentalSerializationApi
val appKoinModule = module {
	single { AppConfig() }
	single { UpsourceClient(get()) }
	single { UpsourceService(get()) }

	single { TeamcityApiClient(get())}
}
