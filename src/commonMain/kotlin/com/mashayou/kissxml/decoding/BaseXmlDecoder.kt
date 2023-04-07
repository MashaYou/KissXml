package com.mashayou.kissxml.decoding

import com.mashayou.kissxml.BooleanCastException
import com.mashayou.kissxml.DecodingException
import com.mashayou.kissxml.IllegalEnumValueException
import com.mashayou.kissxml.parsing.XmlNode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal abstract class BaseXmlDecoder(
    protected val config: XmlDecodingConfig,
    protected val structureDescriptor: SerialDescriptor,
) : AbstractDecoder() {
    /**
     * Root node's siblings index (rootNode.siblingsMap.entries).
     */
    protected var nextElementIndex: Int = 0

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
                val list = currentNode.siblingsMap[currentNode.tag].orEmpty()
                return ListDecoder(
                    list = list,
                    descriptor = descriptor.elementDescriptors.first(),
                    config = config,
                )
            }
            getRootNode().isRoot -> getRootNode().getFirstChild()
            else -> getCurrentNode().getFirstChild()
        } ?: getRootNode()
        return XmlDecoder(
            rootNode = firstChild,
            config = config,
            descriptor = descriptor,
        )
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

    protected abstract fun getCurrentNode() : XmlNode
    protected abstract fun getRootNode() : XmlNode
    protected abstract fun isDecodingDone() : Boolean

    protected fun getValue(kind: SerialKind) : Any {
        val value = getCurrentNode().getContent().orEmpty()

        try {
            return when (kind) {
                PrimitiveKind.INT -> value.toInt()
                PrimitiveKind.LONG -> value.toLong()
                PrimitiveKind.DOUBLE -> value.toDouble()
                PrimitiveKind.SHORT -> value.toShort()
                else -> value
            }
        } catch (e: ClassCastException) {
            throw DecodingException("Cannot decode the value: [$value] like [$kind].")
        }
    }
}