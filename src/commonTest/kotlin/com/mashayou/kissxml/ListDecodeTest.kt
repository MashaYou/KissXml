package com.mashayou.kissxml

import com.mashayou.kissxml.decoding.XmlDecodingConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class ListDecodeTest : StringSpec() {

    private val xml = Xml(
        decodingConfig = XmlDecodingConfig(false),
    )

    init {
        "List decoding with ignore config = true" {
            val input = "<kml><Document><name>Doc2</name></Document><Style>Best</Style><Document><name>Doc1</name></Document></kml>"
            val xml = Xml(decodingConfig = XmlDecodingConfig(true))
            xml.decodeFromString<KmlFile>(input) shouldBe KmlFile(listOf(Document("Doc2"), Document("Doc1")))
        }
        "List of structures decoding" {
            val input = "<kml><Document><name>Doc</name></Document></kml>"
            xml.decodeFromString<KmlFile>(input) shouldBe KmlFile(listOf(Document("Doc")))
        }
        "List of primitives decoding" {
            val input =
                "<Car><feature>First</feature><visible>1</visible><feature>Second</feature></Car>"
            xml.decodeFromString<Car>(input) shouldBe Car(
                feature = listOf("First", "Second"),
                visible = true,
            )
        }
        "example with xml declaration" {
            val input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml><Document><name>Doc</name></Document></kml>"
            xml.decodeFromString<KmlFile>(input) shouldBe KmlFile(listOf(Document("Doc")))
        }
    }

    @Serializable
    private data class Car(
        val feature: List<String>,
        val visible: Boolean,
    )

    @Serializable
    @SerialName("kml")
    private data class KmlFile(
        @SerialName("Document")
        val documents: List<Document>,
    )

    @Serializable
    @SerialName("Document")
    private data class Document(
        val name: String,
    )
}