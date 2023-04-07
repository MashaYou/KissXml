package com.mashayou.kissxml

import com.mashayou.kissxml.decoding.XmlDecodingConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class RealShitDecodeTest : StringSpec() {

    private val xml = Xml(
        decodingConfig = XmlDecodingConfig(false),
    )

    init {
        "Kml file with placemarks + CDATA" {
            val input = """
                <?xml version="1.0" encoding="UTF-8"?>
                <kml xmlns=""><Document> <name></name>
                <Placemark><name>10_71/2-15</name><description></description><Point><coordinates>38.055651652,55.676901324,0</coordinates></Point></Placemark>
                <Placemark><name>10_69/2-30</name><description></description><Point><coordinates>38.055070353,55.676646172,0</coordinates></Point></Placemark>
                <Placemark> <name>Пример с тегами CDATA</name> <description> <![CDATA[ <h1>Теги CDATA имеют смысл!</h1> <p><font color="red">Без ссылок на объекты текст <i>удобнее читать</i> и <b>проще писать</b>.</font></p> ]]> </description> <Point> <coordinates>102.595626,14.996729</coordinates> </Point> </Placemark>
                </Document></kml>
            """.trimIndent()
            val xml = Xml(decodingConfig = XmlDecodingConfig(true))
            xml.decodeFromString<KmlFile>(input) shouldBe KmlFile(
                documents = listOf(
                    Document(
                        name = "", placemarks = listOf(
                            Placemark(
                                name = "10_71/2-15",
                                description = "",
                                point = Point(
                                    coordinates = "38.055651652,55.676901324,0",
                                )
                            ),
                            Placemark(
                                name = "10_69/2-30",
                                description = "",
                                point = Point(
                                    coordinates = "38.055070353,55.676646172,0",
                                )
                            ),
                            Placemark(
                                name = "Пример с тегами CDATA",
                                description = " <![CDATA[ <h1>Теги CDATA имеют смысл!</h1> <p><font color=\"red\">Без ссылок на объекты текст <i>удобнее читать</i> и <b>проще писать</b>.</font></p> ]]> ",
                                point = Point(
                                    coordinates = "102.595626,14.996729",
                                )
                            )
                        )
                    )
                )
            )
        }
        "Kml file with San Francisco placemark" {
            val input = """
                <?xml version="1.0" encoding="UTF-8"?>
                <kml xmlns="http://earth.google.com/kml/2.2">
                <Placemark>
                <name>Test</name>
                <description>Test description</description>
                <Point>
                <coordinates>-122.25585937500001,37.80788523279169,0</coordinates>
                </Point>
                </Placemark>
                </kml>
            """.trimIndent()
            xml.decodeFromString<KmlFile>(input) shouldBe KmlFile(
                placemarks = listOf(
                    Placemark(
                        name = "Test",
                        description = "Test description",
                        point = Point(
                            coordinates = "-122.25585937500001,37.80788523279169,0",
                        ),
                    )
                )
            )
        }
    }

    @Serializable
    @SerialName("kml")
    private data class KmlFile(
        @SerialName("Document")
        val documents: List<Document> = emptyList(),
        @SerialName("Placemark")
        val placemarks: List<Placemark> = emptyList(),
    )

    @Serializable
    private data class Document(
        val name: String,
        @SerialName("Placemark")
        val placemarks: List<Placemark>,
    )

    @Serializable
    private data class Placemark(
        val name: String,
        val description: String,
        @SerialName("Point")
        val point: Point,
    )

    @Serializable
    private data class Point(
        val coordinates: String,
    )
}