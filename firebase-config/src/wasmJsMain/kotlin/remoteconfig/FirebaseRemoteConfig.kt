@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.asJsObject
import dev.gitlive.firebase.js
import dev.gitlive.firebase.remoteconfig.externals.RemoteConfig
import dev.gitlive.firebase.remoteconfig.externals.Settings
import dev.gitlive.firebase.remoteconfig.externals.activate
import dev.gitlive.firebase.remoteconfig.externals.ensureInitialized
import dev.gitlive.firebase.remoteconfig.externals.fetchAndActivate
import dev.gitlive.firebase.remoteconfig.externals.fetchConfig
import dev.gitlive.firebase.remoteconfig.externals.getAll
import dev.gitlive.firebase.remoteconfig.externals.getRemoteConfig
import dev.gitlive.firebase.remoteconfig.externals.getValue
import dev.gitlive.firebase.toJsonElement
import kotlinx.coroutines.await
import kotlinx.datetime.Instant
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

public actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = rethrow { FirebaseRemoteConfig(getRemoteConfig()) }

public actual fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig = rethrow {
    FirebaseRemoteConfig(getRemoteConfig(app.js))
}

public val FirebaseRemoteConfig.js: RemoteConfig get() = js

private fun objectKeys(value: JsAny): JsArray<JsString> = js("Object.keys(value)")

public actual class FirebaseRemoteConfig internal constructor(internal val js: RemoteConfig) {
    public actual val all: Map<String, FirebaseRemoteConfigValue>
        get() = rethrow { getAllKeys().associateWith { getValue(it) } }

    @OptIn(ExperimentalTime::class)
    public actual val info: FirebaseRemoteConfigInfo
        get() = rethrow {
            FirebaseRemoteConfigInfo(
                configSettings = js.settings.toFirebaseRemoteConfigSettings(),
                fetchTime = Instant.fromEpochMilliseconds(js.fetchTimeMillis.toLong()),
                lastFetchStatus = js.lastFetchStatus.toFetchStatus(),
            )
        }

    public actual suspend fun activate(): Boolean = rethrow { activate(js).await() }
    public actual suspend fun ensureInitialized(): Unit = rethrow { ensureInitialized(js).await() }

    public actual suspend fun fetch(minimumFetchInterval: Duration?): Unit = rethrow { fetchConfig(js).await() }

    public actual suspend fun fetchAndActivate(): Boolean = rethrow { fetchAndActivate(js).await() }

    public actual fun getValue(key: String): FirebaseRemoteConfigValue = rethrow {
        FirebaseRemoteConfigValue(getValue(js, key))
    }

    public actual fun getKeysByPrefix(prefix: String): Set<String> =
        getAllKeys().filter { it.startsWith(prefix) }.toSet()

    private fun getAllKeys(): Set<String> {
        return objectKeys(getAll(js)).toList().map { it.toString() }.toSet()
    }

    public actual suspend fun reset() {
        // not implemented for JS target
    }

    public actual suspend fun settings(init: FirebaseRemoteConfigSettings.() -> Unit) {
        val settings = FirebaseRemoteConfigSettings().apply(init)
        js.settings.apply {
            fetchTimeoutMillis = settings.fetchTimeout.inWholeMilliseconds.toJsBigInt()
            minimumFetchIntervalMillis = settings.minimumFetchInterval.inWholeMilliseconds.toJsBigInt()
        }
    }

    public actual suspend fun setDefaults(vararg defaults: Pair<String, Any?>): Unit = rethrow {
        js.defaultConfig = buildJsonObject {
            defaults.forEach {
                put(it.first, it.second.toJsonElement())
            }
        }.asJsObject()
    }

    private fun Settings.toFirebaseRemoteConfigSettings(): FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings(
        fetchTimeout = fetchTimeoutMillis.toLong().milliseconds,
        minimumFetchInterval = minimumFetchIntervalMillis.toLong().milliseconds,
    )

    private fun String.toFetchStatus(): FetchStatus = when (this) {
        "no-fetch-yet" -> FetchStatus.NoFetchYet
        "success" -> FetchStatus.Success
        "failure" -> FetchStatus.Failure
        "throttle" -> FetchStatus.Throttled
        else -> error("Unknown FetchStatus: $this")
    }
}

public actual open class FirebaseRemoteConfigException(code: String, cause: Throwable) : FirebaseException(code, cause)

public actual class FirebaseRemoteConfigClientException(code: String, cause: Throwable) :
    FirebaseRemoteConfigException(code, cause)

public actual class FirebaseRemoteConfigFetchThrottledException(code: String, cause: Throwable) :
    FirebaseRemoteConfigException(code, cause)

public actual class FirebaseRemoteConfigServerException(code: String, cause: Throwable) :
    FirebaseRemoteConfigException(code, cause)

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: JsException) {
        throw FirebaseRemoteConfigServerException(e.message ?: "", e)
    } catch (e: Exception) {
        throw e
    }
}
