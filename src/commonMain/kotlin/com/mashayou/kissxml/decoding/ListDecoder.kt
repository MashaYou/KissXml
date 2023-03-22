package com.mashayou.kissxml.decoding

import com.mashayou.kissxml.DecodingException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal class ListDecoder(
    val list: List<String>,
    val descriptor: SerialDescriptor,
) : AbstractDecoder() {

    private var nextElementIndex = 0
    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun decodeValue(): Any {
        val currentValue = list[nextElementIndex - 1]
        try {
            return when (descriptor.kind) {
                PrimitiveKind.INT -> currentValue.toInt()
                PrimitiveKind.LONG -> currentValue.toLong()
                PrimitiveKind.DOUBLE -> currentValue.toDouble()
                PrimitiveKind.SHORT -> currentValue.toShort()
                else -> currentValue
            }
        } catch (e: ClassCastException) {
            throw DecodingException("Cannot decode the value: [$currentValue] like [${descriptor.kind}].")
        }
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (nextElementIndex == list.size) return CompositeDecoder.DECODE_DONE
        return nextElementIndex++
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor) = list.size
}