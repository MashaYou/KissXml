package com.mashayou.kissxml.decoding

/**
 * A config to change parsing behavior.
 * @property ignoreUnknownTags Whether to allow/prohibit unknown tags during the deserialization
 */
public data class XmlDecodingConfig(
    val ignoreUnknownTags: Boolean,
)