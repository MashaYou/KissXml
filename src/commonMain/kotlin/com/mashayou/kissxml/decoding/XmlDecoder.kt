package com.mashayou.kissxml.decoding


import com.mashayou.kissxml.BooleanCastException
import com.mashayou.kissxml.DecodingException
import com.mashayou.kissxml.IllegalEnumValueException
import com.mashayou.kissxml.UnknownTagException
import com.mashayou.kissxml.parsing.XmlNode
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * @param nextElementIndex rootNode siblings index.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class XmlDecoder(
    private val rootNode: XmlNode,
    private val config: XmlDecodingConfig = XmlDecodingConfig(true),
    private val descriptior: SerialDescriptor,
    private var nextElementIndex: Int = 0,
) : AbstractDecoder() {
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
                descriptior = deserializer.descriptor,
            )
            return decoder.decodeSerializableValue(deserializer)
        }
    }

    override val serializersModule: SerializersModule = EmptySerializersModule()

    /**
     * Decoding entry point.
     * Returns a new instance of the XmlDecoder, so that each structure that is being
     * recursively decoded keeps track of its own elementIndex state separately.
     */
    override fun beginStructure(descriptor: SerialDescriptor): XmlDecoder {
        val firstChild = if (rootNode.isRoot) {
            rootNode.getFirstChild()
        } else {
            getCurrentNode().getFirstChild()
        } ?: rootNode
        return XmlDecoder(firstChild, config, descriptor)
    }

    /**
     * Returns the next index of a deserialized value. Always calls inside structure.
     * Check if decoding done and increase [nextElementIndex] by one.
     * @param descriptor structure's despriptor
     */
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (isDecodingDone()) {
            return CompositeDecoder.DECODE_DONE
        }

        val nextNode = rootNode.getSiblings().elementAt(nextElementIndex)
        val nextIndex = descriptor.getElementIndex(nextNode.tag)

        if (nextIndex == CompositeDecoder.UNKNOWN_NAME) {
            throw UnknownTagException(nextNode.tag, nextNode.getParent()?.tag)
        }
        nextElementIndex++
        return nextIndex
    }
    override fun decodeValue(): Any {
        val primitiveKind = descriptior.getElementDescriptor(nextElementIndex - 1).kind
        val value = getCurrentNode().getContent()
            ?: throw DecodingException("Cannot decode the value: [null] like [${primitiveKind}].")
        try {
            return when (primitiveKind) {
                PrimitiveKind.INT -> value.toInt()
                PrimitiveKind.LONG -> value.toLong()
                PrimitiveKind.DOUBLE -> value.toDouble()
                else -> value
            }
        } catch (e: ClassCastException) {
            throw DecodingException("Cannot decode the value: [$value] like [${primitiveKind}].")
        }
    }

    override fun decodeBoolean(): Boolean {
        val primitiveNode = getCurrentNode()
        return when (val value = primitiveNode.getContent()) {
            "1", "true" -> true
            "0", "false" -> false
            else -> {
                throw BooleanCastException(value, primitiveNode.tag)
            }
        }
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val value = decodeValue().toString()
        val index = enumDescriptor.getElementIndex(value)

        if (index == CompositeDecoder.UNKNOWN_NAME) {
            throw IllegalEnumValueException(value, getCurrentNode().tag)
        }

        return index
    }

    private fun isDecodingDone() = rootNode.isRoot || nextElementIndex == rootNode.getSiblings().size
    private fun getCurrentNode(): XmlNode = rootNode.getSiblings().elementAt(nextElementIndex - 1)
}