package com.mashayou.kissxml

import com.mashayou.kissxml.parsing.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class ParserTest : FunSpec() {

    private val parser = Parser()

    init {
        context("Parsing tests") {
            withData<TestCase>(
                nameFn = { it.input },
                TestCase(
                    input = "<cat><name>Барабулька</name></cat>",
                    expected = XmlNode(
                        tag = "cat",
                    ).apply {
                        addChildNode(
                            XmlNode(
                                tag = "name",
                                content = "Барабулька",
                            )
                        )
                    }
                ),
                TestCase(
                    input = "<cat><name>Барабулька\nОтличная кошка\nНО! УБИЙЦА</name></cat>",
                    expected = XmlNode(
                        tag = "cat",
                    ).apply {
                        addChildNode(
                            XmlNode(
                                tag = "name",
                                content = "Барабулька\n" +
                                        "Отличная кошка\n" +
                                        "НО! УБИЙЦА",
                            )
                        )
                    }
                ),
                TestCase(
                    input = "<cat><name>Барабулька\nОтличная кошка\nНО! УБИЙЦА</name><name>ASSASSIN_CAT</name></cat>",
                    expected = XmlNode(
                        tag = "cat",
                    ).apply {
                        addChildNode(
                            XmlNode(
                                tag = "name",
                                content = "Барабулька\n" +
                                        "Отличная кошка\n" +
                                        "НО! УБИЙЦА",
                            )
                        )
                        addChildNode(
                            XmlNode(
                                tag = "name",
                                content = "ASSASSIN_CAT"
                            )
                        )
                    }
                ),
                TestCase(
                    input = "<Folder><Folder>Барабулька\nОтличная кошка\nНО! УБИЙЦА</Folder><Folder><![CDATA[<brr>ASSASSIN_CAT</brr>]]></Folder></Folder>",
                    expected = XmlNode(
                        tag = "Folder",
                    ).apply {
                        addChildNode(
                            XmlNode(
                                tag = "Folder",
                                content = "Барабулька\n" +
                                        "Отличная кошка\n" +
                                        "НО! УБИЙЦА",
                            )
                        )
                        addChildNode(
                            XmlNode(
                                tag = "Folder",
                                content = "<![CDATA[<brr>ASSASSIN_CAT</brr>]]>",
                            )
                        )
                    }
                ),
                TestCase(
                    input = "<A><B><C>Барабулька\nОтличная кошка\nНО! УБИЙЦА</C></B><B><C>ASSASSIN_CAT</C></B></A>",
                    expected = XmlNode(
                        tag = "A",
                    ).apply {
                        addChildNode(
                            XmlNode(
                                tag = "B",
                            ).apply {
                                addChildNode(
                                    XmlNode(
                                        tag = "C",
                                        content = "Барабулька\n" +
                                                "Отличная кошка\n" +
                                                "НО! УБИЙЦА",
                                    )
                                )
                            }
                        )
                        addChildNode(
                            XmlNode(
                                tag = "B",
                            ).apply {
                                addChildNode(
                                    XmlNode(
                                        tag = "C",
                                        content = "ASSASSIN_CAT",
                                    )
                                )
                            }
                        )
                    }
                ),
                TestCase(
                    input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<A><B>Барабулька</C></A>",
                    expected = XmlNode(
                        tag = "A",
                    ).apply {
                        addChildNode(
                            XmlNode(
                                tag = "B",
                                content = "Барабулька",
                            )
                        )
                    }
                ),
                TestCase(
                    input = "<A color=\"threeColor\" eyes=\"two\"><B color=\"qwerty\">Барабулька</B></A>",
                    expected = XmlNode(
                        tag = "A",
                    ).apply {
                        addChildNode(
                            XmlNode(
                                tag = "B",
                                content = "Барабулька",
                            ).apply {
                                addAttribute("color" to "qwerty")
                            }
                        )
                        addAttributes(mapOf("color" to "threeColor", "eyes" to "two"))
                    }
                ),
                TestCase("<name>Барабулька</name>", XmlNode(tag = "name", content = "Барабулька")),
            ) { (input, expected) ->
                parser.getAbstractTree(input) shouldBe expected
            }
        }
    }

    private data class TestCase(
        val input: String,
        val expected: XmlNode,
    )
}

