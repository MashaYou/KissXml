package com.mashayou.kissxml

import com.mashayou.kissxml.decoding.XmlDecodingConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class DecoderTest : FunSpec() {

    private val xml = Xml(
        decodingConfig = XmlDecodingConfig(true),
    )

    init {
        context("Primitives, embedded structures decoding") {
            withData<TestCase>(
                nameFn = { it.input },
                listOf(
                    TestCase(
                        input = "<folder><Name>StudioProjects</Name></folder>",
                        expected = Folder("StudioProjects"),
                    ),
                    TestCase(
                        input = "<folder><Name>StudioProjects</Name><description>Test description</description></folder>",
                        expected = Folder("StudioProjects", "Test description"),
                    ),
                    TestCase(
                        input = "<folder><description>Test description</description><Name>StudioProjects</Name></folder>",
                        expected = Folder("StudioProjects", "Test description"),
                    ),
                    TestCase(
                        input = "<folder><Name>A</Name><Placemark><color>Red</color><code>15.23</code><visible>1</visible></Placemark></folder>",
                        expected = Folder("A", placemark =  Placemark("Red", 15.23, true)),
                    ),
                    TestCase(
                        input = "<folder><Name>Documents</Name><folder><Name>Inner</Name><Placemark><color>Red</color><code>15.23</code><visible>1</visible></Placemark></folder></folder>",
                        expected = Folder("Documents", folder = Folder("Inner", placemark =  Placemark("Red", 15.23, true))),
                    ),
                )
            ) { (input, expected) ->
                xml.decodeFromString<Folder>(input) shouldBe expected
            }
        }
        context("Enum decoding") {
            withData<TestCase>(
                nameFn = { it.input },
                listOf(
                    TestCase(
                        input = "<folder><Name>Documents</Name><Placemark><color>Red</color><code>15.23</code><visible>1</visible><type>A</type></Placemark></folder>",
                        expected = Folder("Documents", placemark =  Placemark("Red", 15.23, true, type = Type.A)),
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
        @SerialName("Name")
        val name: String,
        val description: String? = null,
        @SerialName("Placemark")
        val placemark: Placemark? = null,
        val folder: Folder? = null,
    )

    @Serializable
    @SerialName("Placemark")
    private data class Placemark(
        val color: String,
        val code: Double,
        val visible: Boolean,
        val type: Type? = null,
    )

    private enum class Type {
        A,
        B,
        C
    }
}