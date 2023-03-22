package com.mashayou.kissxml

import com.mashayou.kissxml.decoding.XmlDecodingConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class ListsDecodeTest : FunSpec() {

    private val xml = Xml(
        decodingConfig = XmlDecodingConfig(true),
    )

    init {
        context("List decoding") {
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
    }

    @Serializable
    private data class Car(
        val feature: List<String>,
        val visible: Boolean,
    )

    private data class TestCase(
        val input: String,
        val expected: Car,
    )
}