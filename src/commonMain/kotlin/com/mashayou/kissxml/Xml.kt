package com.mashayou.kissxml

import com.mashayou.kissxml.decoding.XmlDecoder
import com.mashayou.kissxml.decoding.XmlDecodingConfig
import com.mashayou.kissxml.parsing.Parser
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

public open class Xml(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    private val decodingConfig: XmlDecodingConfig,
) : StringFormat {

    private val parser = Parser()
    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        val tree = parser.getAbstractTree(
            input = string,
        )
        return XmlDecoder.decode(
            deserializer = deserializer,
            config = decodingConfig,
            root = tree,
        )
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        TODO("I'll do it later")
    }

    /**
     * The default instance of [Xml] with the default configuration.
     * See [XmlConfig] for the list of the default options
     */
    public companion object Default : Xml(
        decodingConfig = XmlDecodingConfig(false),
    )
}