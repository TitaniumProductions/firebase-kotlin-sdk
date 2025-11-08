@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.gitlive.firebase

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

public val json: Json = Json {
    ignoreUnknownKeys = true
    serializersModule = anySerializersModule
}

private val anySerializersModule = SerializersModule {
    polymorphic(Any::class) {
        subclass(String::class, String.serializer())
        subclass(Double::class, Double.serializer())
        subclass(Long::class, Long.serializer())
        subclass(Int::class, Int.serializer())
        subclass(Boolean::class, Boolean.serializer())
    }
}

public inline fun <reified T> T.asJsObject(): JsAny = stringToJsObject(json.encodeToString(this))

public fun stringToJsObject(string: String): JsAny = js("JSON.parse(string)")
