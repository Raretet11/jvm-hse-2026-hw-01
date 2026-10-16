package org.example.export

import org.example.model.Product
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.w3c.dom.Element

class XmlProductExporter : Exporter {
    override val format = "XML"

    override fun export(products: List<Product>, outputPath: String) {
        val document = buildDocument(products)
        save(document, outputPath)
    }

    private fun buildDocument(products: List<Product>): Document {
        val doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .newDocument()

        val root = doc.createElement("products")
        doc.appendChild(root)

        products.forEach { product ->
            root.appendChild(buildProductElement(doc, product))
        }

        return doc
    }

    private fun buildProductElement(doc: Document, product: Product): Element {
        val productEl = doc.createElement("product")

        productEl.appendChild(textElement(doc, "id", product.id.toString()))
        productEl.appendChild(textElement(doc, "name", product.name))
        productEl.appendChild(textElement(doc, "type", product.type.name.lowercase()))

        val priceEl = textElement(doc, "price", product.price.amount.toString())
        priceEl.setAttribute("currency", product.price.currency.name)
        productEl.appendChild(priceEl)

        val weightEl = textElement(doc, "weight", product.weight.amount.toString())
        weightEl.setAttribute("unit", product.weight.unit.name)
        productEl.appendChild(weightEl)

        productEl.appendChild(textElement(doc, "vehicle", product.vehicle.name))

        val extraEl = doc.createElement("extra")
        product.extra.forEach { (key, value) ->
            val paramEl = textElement(doc, "param", value)
            paramEl.setAttribute("key", key)
            extraEl.appendChild(paramEl)
        }
        productEl.appendChild(extraEl)

        return productEl
    }

    private fun textElement(doc: Document, tag: String, text: String): Element {
        val el = doc.createElement(tag)
        el.appendChild(doc.createTextNode(text))
        return el
    }

    private fun save(document: Document, outputPath: String) {
        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        }

        transformer.transform(
            DOMSource(document),
            StreamResult(File(outputPath))
        )
    }
}
