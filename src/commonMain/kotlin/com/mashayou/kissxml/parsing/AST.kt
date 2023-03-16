package com.mashayou.kissxml.parsing

/**
 * Node of Abstract Syntax Tree (AST). Every node has xml tag equivalent.
 */
internal data class XmlNode private constructor(
    val tag: String,
    private var content: String?,
    private val children: MutableList<XmlNode>,
    private val attributes: MutableMap<String, String>,
) {
    private var parent: XmlNode? = null
    constructor(
        tag: String,
        content: String? = null
    ) : this(
        tag = tag,
        content = content,
        children = mutableListOf(),
        attributes = mutableMapOf(),
    )

    val isPrimitive by lazy {
        children.size == 0 && content != null
    }

    val isRoot by lazy {
        parent == null
    }

    val isStructure by lazy {
        content == null
    }

    fun getParent(): XmlNode? {
        return parent
    }

    fun setContent(content: String) {
        this.content = content
    }

    fun getContent(): String? {
        return content
    }

    /**
     * Use this fun to add child, not constructor!
     */
    fun addChildNode(child: XmlNode) {
        child.parent = this
        children.add(child)
    }

    /**
     * Use this fun to add children, not constructor!
     */
    fun addChildren(newChildren: List<XmlNode>) {
        newChildren.forEach {
            children.add(it)
        }
    }

    fun getChildrenNodes() = children

    /**
     * @return all neighbours/siblings (all children of current node's parent)
     */
    fun getSiblings(): List<XmlNode> = parent?.children ?: listOf(this)

    fun getFirstChild() = children.elementAtOrNull(0)

    fun addAttribute(pair: Pair<String, String>) {
        attributes[pair.first] = pair.second
    }

    fun addAttributes(map: Map<String, String>) {
        attributes.putAll(map)
    }

    fun getAttributes(): Map<String, String> {
        return attributes
    }
}