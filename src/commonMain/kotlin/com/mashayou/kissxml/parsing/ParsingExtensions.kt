package com.mashayou.kissxml.parsing

internal fun String.removeXmlDeclaration(): String {
    return this.replaceFirst(Regex("<\\?xml.*\\?>"), "")
}

internal fun String.getContentBetweenTags(tag: String): String {
    return this.replace(Regex("^[^<]*${getOpenTagPatternWithAttr(tag)}"), "")
        .replace(Regex("${getCloseTagPattern(tag)}[^<>]*\$"), "")
}

internal fun String.getStartTag(withAttributes: Boolean = true): String? {
    val pattern = if (withAttributes) getOpenTagPatternWithAttr() else getOpenTagPattern()
    val regex = Regex(pattern)
    return regex.find(this, 0)
        ?.value?.removeBrackets()?.trim()
}

internal fun String.getEndTag(): String? {
    val regex = Regex(getCloseTagPattern())
    return regex.find(this, 0)
        ?.value?.removeBrackets()
}

internal fun String.getAttributes(): Map<String, String> {
    val regex = Regex("[a-zA-Z0-9]+=\"[^\\s><]+\"")
    val result = mutableMapOf<String, String>()
    regex.findAll(this)
        .forEach { match ->
            val attr = match.value
                .split('=', '\"')
                .filter {
                    it.isNotBlank() && it.isNotEmpty()
                }
            if (attr.size == 2) {
                result[attr[0]] = attr[1]
            }
        }
    return result
}

private fun String.removeBrackets() = this.replace(Regex("<|>|/")) { "" }

private fun getOpenTagPatternWithAttr(tag: String = "[a-zA-Z0-9]+") = "<$tag[^>]*>"

private fun getOpenTagPattern(tag: String = "[a-zA-Z0-9]+") = "<$tag"

private fun getCloseTagPattern(tag: String = "[a-zA-Z0-9]+") = "</$tag>"