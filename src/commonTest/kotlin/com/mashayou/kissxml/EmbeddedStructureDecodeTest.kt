package com.mashayou.kissxml

import com.mashayou.kissxml.decoding.XmlDecodingConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class EmbeddedStructureDecodeTest : FunSpec() {

    private val xml = Xml(
        decodingConfig = XmlDecodingConfig(true),
    )

    init {
        context("Embedded structures decoding") {
            withData<TestCase>(
                nameFn = { it.input },
                listOf(
                    TestCase(
                        input = "<folder><folder><Placemark><color>Red</color></Placemark></folder><Placemark><color>Blue</color></Placemark></folder>",
                        expected = Folder(
                            placemark = Placemark("Blue"),
                            folder = Folder(
                                placemark = Placemark("Red")
                            ),
                        ),
                    ),
                    TestCase(
                        input = "<folder><folder><Placemark><color>Red</color></Placemark><folder><Placemark><color>Grey</color></Placemark></folder></folder><Placemark><color>Blue</color></Placemark></folder>",
                        expected = Folder(
                            placemark = Placemark("Blue"),
                            folder = Folder(
                                placemark = Placemark("Red"),
                                folder = Folder(
                                    placemark = Placemark("Grey"),
                                )
                            ),
                        ),
                    ),
                )
            ) { (input, expected) ->
                xml.decodeFromString<Folder>(input) shouldBe expected
            }
        }
    }

    private data class TestCase(
        val input: String,
        val expected: Folder,
    )

    @Serializable
    @SerialName("folder")
    private data class Folder(
        @SerialName("Placemark")
        val placemark: Placemark,
        val folder: Folder? = null,
    )

    @Serializable
    private data class Placemark(
        val color: String,
    )
}