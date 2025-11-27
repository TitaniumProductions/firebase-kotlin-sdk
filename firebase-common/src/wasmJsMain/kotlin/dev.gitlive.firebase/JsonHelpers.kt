@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.gitlive.firebase

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

public fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Map<*, *> -> buildJsonObject {
        forEach { (key, value) ->
            if (key is String) {
                put(key, value.toJsonElement())
            }
        }
    }
    is Iterable<*> -> buildJsonArray {
        forEach { value ->
            add(value.toJsonElement())
        }
    }
    is Array<*> -> buildJsonArray {
        forEach { value ->
            add(value.toJsonElement())
        }
    }
    else -> JsonPrimitive(toString())
}

public val json: Json = Json {
    ignoreUnknownKeys = true
}

public inline fun <reified T> T.asJsObject(): JsAny = stringToJsObject(json.encodeToString(this))

public fun stringToJsObject(string: String): JsAny = js("JSON.parse(string)")
