package com.mashayou.kissxml

internal fun String.prepareToAssert(): String {
    return this.trimIndent()
        .replace("\\s+", " ")
        .replace("\t", "")
        .replace("\n", "")
}