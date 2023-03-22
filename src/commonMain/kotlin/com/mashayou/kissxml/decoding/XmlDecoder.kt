package com.mashayou.kissxml.decoding


import com.mashayou.kissxml.UnknownTagException
import com.mashayou.kissxml.parsing.XmlNode
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder

/**
 * Main XML decoder for structures and primitives.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class XmlDecoder(
    private val rootNode: XmlNode,
    config: XmlDecodingConfig = XmlDecodingConfig(true),
    descriptor: SerialDescriptor,
) : BaseXmlDecoder(
    config = config,
    structureDescriptor = descriptor
) {
    companion object {
        /**
         * @param deserializer - deserializer provided by Kotlin compiler
         * @param root - root node for decoding (created after parsing)
         * @return decoded (deserialized) object of type T
         */
        fun <T> decode(
            deserializer: DeserializationStrategy<T>,
            config: XmlDecodingConfig,
            root: XmlNode,
        ): T {
            val decoder = XmlDecoder(
                rootNode = root,
                config = config,
                descriptor = deserializer.descriptor,
            )
            return decoder.decodeSerializableValue(deserializer)
        }
    }

    /**
     * Returns the next index of a deserialized value. Always calls inside the structure.
     * Check if decoding done and increase [nextElementIndex] by one.
     * @param descriptor structure's descriptor
     */
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (isDecodingDone()) {
            return CompositeDecoder.DECODE_DONE
        }

        val nextElement = rootNode.siblingsMap.entries.elementAt(nextElementIndex).value.first()
        val nextIndex = descriptor.getElementIndex(nextElement.tag)

        if (nextIndex == CompositeDecoder.UNKNOWN_NAME) {
            throw UnknownTagException(nextElement.tag, nextElement.getParent()?.tag)
        }
        nextElementIndex++
        return nextIndex
    }

    override fun decodeValue() = getValue(
        kind = structureDescriptor
            .getElementDescriptor(nextElementIndex - 1)
            .kind
    )

    override fun isDecodingDone() = rootNode.isRoot || nextElementIndex == rootNode.siblingsMap.size
    override fun getCurrentNode() = rootNode.siblingsMap.entries.elementAt(nextElementIndex - 1).value.first()
    override fun getRootNode() = rootNode
}