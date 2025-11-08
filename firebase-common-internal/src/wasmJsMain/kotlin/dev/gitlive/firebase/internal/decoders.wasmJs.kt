package dev.gitlive.firebase.internal

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder

internal actual fun FirebaseDecoderImpl.structureDecoder(
    descriptor: SerialDescriptor,
    polymorphicIsNested: Boolean
): CompositeDecoder {
    TODO("Not yet implemented")
}

internal actual fun getPolymorphicType(value: Any?, discriminator: String): String {
    TODO("Not yet implemented")
}