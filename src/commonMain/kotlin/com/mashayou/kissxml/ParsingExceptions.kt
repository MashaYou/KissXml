@file:Suppress("HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE", "MISSING_KDOC_TOP_LEVEL")
package com.mashayou.kissxml

import kotlinx.serialization.SerializationException

internal sealed class XmlDecodingException(message: String) : SerializationException(message)

internal class ParseException(message: String) : XmlDecodingException(message)

internal class DecodingException(message: String) : XmlDecodingException(message)

internal class UnknownTagException(tag: String, parentTag: String?) : XmlDecodingException(
    "Unknown tag received: <$tag> in scope <$parentTag>." +
            " Switch the configuration option: 'XmlConfig.ignoreUnknownNames'" +
            " to true if you would like to skip unknown tags."
)

internal class IllegalEnumValueException(value: String, tag: String) : XmlDecodingException(
    "Illegal enum value for tag <$tag>: $value."
)

internal class BooleanCastException(value: String?, tag: String) : XmlDecodingException(
    "Illegal boolean value for tag <$tag>: $value."
)
