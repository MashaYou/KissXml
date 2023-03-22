package com.mashayou.kissxml

import com.mashayou.kissxml.decoding.XmlDecodingConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class ListDecodeTest : FunSpec() {

    private val xml = Xml(
        decodingConfig = XmlDecodingConfig(true),
    )

    init {
        context("List of primitives decoding") {
            withData<TestCase>(
                nameFn = { it.input },
                listOf(
                    TestCase(
                        input = "<Car><feature>First</feature><visible>1</visible><feature>Second</feature></Car>",
                        expected = Car(feature = listOf("First", "Second"), visible = true),
                    ),
                )
            ) { (input, expected) ->
                xml.decodeFromString<Car>(input) shouldBe expected
            }
        }
        context("List of structures decoding") {
            withData<TestCase2>(
                nameFn = { it.input },
                listOf(
                    TestCase2(
                        input = "<Ferrari><Wheel><diameter>111</diameter></Wheel></Ferrari>",
                        expected = Ferrari(wheels = listOf(Wheel(111))),
                    ),
                )
            ) { (input, expected) ->
                xml.decodeFromString<Ferrari>(input) shouldBe expected
            }
        }
    }

    @Serializable
    private data class Car(
        val feature: List<String>,
        val visible: Boolean,
    )

    @Serializable
    private data class Ferrari(
        @SerialName("Wheel")
        val wheels: List<Wheel>
    )

    @Serializable
    private data class Wheel (
        val diameter: Int,
    )

    private data class TestCase(
        val input: String,
        val expected: Car,
    )

    private data class TestCase2(
        val input: String,
        val expected: Ferrari,
    )
}