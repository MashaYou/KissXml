package com.mashayou.kissxml.parsing

import com.mashayou.kissxml.*

/**
 * Parse String to Abstract Syntax Tree (AST), see [AbstractNode].
 */
internal class Parser {
    fun getAbstractTree(input: String, rootTag: String? = null): XmlNode {
        val rawXml = input.removeXmlDeclaration()
        val tagWithAttributes = rootTag ?: rawXml.getStartTag(withAttributes = true)

        val tag = tagWithAttributes?.split(" ")?.get(0)
            ?: throw ParseException("No xml tags found in input.")

        val root = XmlNode(
            tag = tag,
        ).apply {
            addAttributes(tagWithAttributes.getAttributes())
        }

        getTree(
            tokens = Regex("(?=<)|(?<=>)")
                .split(rawXml.getContentBetweenTags(tag))
                .filterNot { it.isEmpty() },
            root = root,
        )

        return root
    }

    private fun getTree(tokens: List<String>, root: XmlNode) {
        var deep = 0
        var ignore = false

        var currentNode: XmlNode = root
        var currentNodeStartIndex: Int? = null

        for ((index, token) in tokens.withIndex()) {
            if (token.isBlank() || token.isEmpty()) continue

            when {
                token.contains("<![CDATA[") -> ignore = true
                token.contains("]]>") -> ignore = false
            }
            if (ignore) continue

            val startTag = token.getStartTag(withAttributes = false)
            val endTag = token.getEndTag()
            when {
                startTag != null -> {
                    if (deep == 0) {
                        val newNode = XmlNode(
                            tag = startTag,
                        ).apply {
                            addAttributes(token.getAttributes())
                        }
                        currentNode.addChildNode(newNode)
                        currentNode = newNode
                        currentNodeStartIndex = index
                    }

                    deep++
                }

                endTag != null -> {
                    deep--

                    if (deep == 0) {
                        currentNodeStartIndex?.let { nodeStartIndex ->
                            val subTokens = tokens.subList(
                                fromIndex = nodeStartIndex + 1,
                                toIndex = index,
                            )
                            if (subTokens.isNotEmpty()) {
                                getTree(
                                    tokens = subTokens,
                                    root = currentNode,
                                )
                            }
                            currentNode.getParent()?.let { currentNode = it }
                            currentNodeStartIndex = null
                        }
                            ?: throw ParseException("There is no open tag for the end tag: <\\/$endTag>.")
                    }
                }

                else -> {
                    // just skip
                }
            }
        }
        if (currentNode === root && root.getChildrenNodes().isEmpty()) {
            root.setContent(tokens.joinToString(separator = ""))
        }
    }
}

