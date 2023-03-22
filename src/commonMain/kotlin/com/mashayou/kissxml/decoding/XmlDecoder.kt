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
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * Main XML decoder for structures and primitives.
 * @param nextElementIndex rootNode siblings index (rootNode.siblingsMap.entries).
 */
@OptIn(ExperimentalSerializationApi::class)
internal open class XmlDecoder(
    private val rootNode: XmlNode,
    private val config: XmlDecodingConfig = XmlDecodingConfig(true),
    private val currentStructureDescriptior: SerialDescriptor,
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
                currentStructureDescriptior = deserializer.descriptor,
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
    override fun beginStructure(descriptor: SerialDescriptor): AbstractDecoder {
        val firstChild = when {
            descriptor.kind == StructureKind.LIST -> {
                val currentNode = getCurrentNode()
                val list = currentNode.siblingsMap[currentNode.tag].orEmpty().mapNotNull { it.getContent() }
                return ListDecoder(
                    list = list,
                    descriptor = descriptor.elementDescriptors.first(),
                )
            }
            rootNode.isRoot -> rootNode.getFirstChild()
            else -> getCurrentNode().getFirstChild()
        } ?: rootNode
        return XmlDecoder(
            rootNode = firstChild,
            config = config,
            currentStructureDescriptior = descriptor,
        )
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

        val nextElement = rootNode.siblingsMap.entries.elementAt(nextElementIndex).value.first()
        val nextIndex = descriptor.getElementIndex(nextElement.tag)

        if (nextIndex == CompositeDecoder.UNKNOWN_NAME) {
            throw UnknownTagException(nextElement.tag, nextElement.getParent()?.tag)
        }
        nextElementIndex++
        return nextIndex
    }
    override fun decodeValue(): Any {
        val primitiveKind = currentStructureDescriptior
            .getElementDescriptor(nextElementIndex - 1)
            .kind
        val value = getCurrentNode().getContent()
            ?: throw DecodingException("Cannot decode the value: [null] like [${primitiveKind}].")
        try {
            return when (primitiveKind) {
                PrimitiveKind.INT -> value.toInt()
                PrimitiveKind.LONG -> value.toLong()
                PrimitiveKind.DOUBLE -> value.toDouble()
                PrimitiveKind.SHORT -> value.toShort()
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

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        val currentNode = getCurrentNode()
        val list = currentNode.siblingsMap[currentNode.tag].orEmpty()
        return list.size
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val value = decodeValue().toString()
        val index = enumDescriptor.getElementIndex(value)

        if (index == CompositeDecoder.UNKNOWN_NAME) {
            throw IllegalEnumValueException(value, getCurrentNode().tag)
        }

        return index
    }

    private fun isDecodingDone() =
        // we iterate all over structure and return to start point
        rootNode.isRoot
                // internal structure ends
            || nextElementIndex == rootNode.siblingsMap.size
    private fun getCurrentNode() = rootNode.siblingsMap.entries.elementAt(nextElementIndex - 1).value.first()
}