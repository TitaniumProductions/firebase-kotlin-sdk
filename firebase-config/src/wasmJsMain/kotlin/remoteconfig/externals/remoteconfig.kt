@file:OptIn(ExperimentalWasmJsInterop::class)
@file:JsModule("firebase/remote-config")

package dev.gitlive.firebase.remoteconfig.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun activate(remoteConfig: RemoteConfig): Promise<JsBoolean>

public external fun ensureInitialized(remoteConfig: RemoteConfig): Promise<Nothing>

public external fun fetchAndActivate(remoteConfig: RemoteConfig): Promise<JsBoolean>

public external fun fetchConfig(remoteConfig: RemoteConfig): Promise<Nothing>

public external fun getAll(remoteConfig: RemoteConfig): JsAny

public external fun getBoolean(remoteConfig: RemoteConfig, key: String): Boolean

public external fun getNumber(remoteConfig: RemoteConfig, key: String): JsNumber

public external fun getRemoteConfig(app: FirebaseApp? = definedExternally): RemoteConfig

public external fun getString(remoteConfig: RemoteConfig, key: String): String?

public external fun getValue(remoteConfig: RemoteConfig, key: String): Value

public external interface RemoteConfig {
    public var defaultConfig: JsAny
    public var fetchTimeMillis: Double
    public var lastFetchStatus: String
    public val settings: Settings
}

public external interface Settings {
    public var fetchTimeoutMillis: JsBigInt
    public var minimumFetchIntervalMillis: JsBigInt
}

public external interface Value {
    public fun asBoolean(): Boolean
    public fun asNumber(): JsNumber
    public fun asString(): String?
    public fun getSource(): String
}
