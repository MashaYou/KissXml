package com.mashayou.kissxml

import com.mashayou.kissxml.decoding.XmlDecodingConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class EnumDecodeTest : FunSpec() {

    private val xml = Xml(
        decodingConfig = XmlDecodingConfig(true),
    )

    init {
        context("Enum decoding") {
            withData<TestCase>(
                nameFn = { it.input },
                listOf(
                    TestCase(
                        input = "<Point><type>A</type></Point>",
                        expected = Point(type = Type.A),
                    ),
                )
            ) { (input, expected) ->
                xml.decodeFromString<Point>(input) shouldBe expected
            }
        }
    }

    @Serializable
    private data class Point(
        val type: Type,
    )

    private enum class Type {
        A,
        B,
        C
    }

    private data class TestCase(
        val input: String,
        val expected: Point,
    )
}