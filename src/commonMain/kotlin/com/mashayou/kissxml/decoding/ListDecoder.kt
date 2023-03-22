package com.mashayou.kissxml.decoding

import com.mashayou.kissxml.parsing.XmlNode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

internal class ListDecoder(
    val list: List<XmlNode>,
    descriptor: SerialDescriptor,
    config: XmlDecodingConfig,
) : BaseXmlDecoder(config, descriptor) {

    override val serializersModule: SerializersModule = EmptySerializersModule()
    override fun getCurrentNode() = list[nextElementIndex - 1]
    override fun getRootNode() = getCurrentNode()
    override fun isDecodingDone(): Boolean = nextElementIndex == list.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (isDecodingDone()) return CompositeDecoder.DECODE_DONE
        return nextElementIndex++
    }
    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeValue() = getValue(kind = structureDescriptor.kind)

    override fun decodeCollectionSize(descriptor: SerialDescriptor) = list.size
}