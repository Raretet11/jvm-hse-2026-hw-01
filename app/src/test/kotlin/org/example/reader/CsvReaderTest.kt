package org.example.reader

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class CsvReaderTest {
    @TempDir
    lateinit var tempDir: Path

    @Test fun readsRowWithoutQuotes() {
        val rows = readerFor("2,Castrol Magnatec,oil,300.5руб,1.2kg,PASSENGER,volume=1").readAll()

        assertEquals(
            listOf("2", "Castrol Magnatec", "oil", "300.5руб", "1.2kg", "PASSENGER", "volume=1"),
            rows.single(),
        )
    }

    @Test fun keepsCommaInsideQuotes() {
        val rows = readerFor("""1,"Mobil 1, Synthetic",oil,$12.5,10lb,PASSENGER,volume=4""").readAll()

        assertEquals(7, rows.single().size)
        assertEquals("Mobil 1, Synthetic", rows.single()[1])
    }

    @Test fun unescapesDoubledQuotes() {
        val rows = readerFor("6,\"Bosch, \"\"Air Filter\"\"\",accessory,\$7.5,0.05kg,MOTORCYCLE,color=белый")
            .readAll()

        assertEquals("Bosch, \"Air Filter\"", rows.single()[1])
    }

    @Test fun skipsHeader() {
        val reader = readerFor("2,Castrol Magnatec,oil,300.5руб,1.2kg,PASSENGER,volume=1")
        val rows = reader.readAll()

        assertEquals(HEADER.split(","), reader.header)
        assertEquals(1, rows.size)
    }

    private fun readerFor(vararg rows: String): CsvReader {
        val content = (listOf(HEADER) + rows).joinToString("\n")
        val path = tempDir.resolve("input.csv")
        path.toFile().writeText(content, Charsets.UTF_8)
        return CsvReader(path.toString())
    }

    private companion object {
        const val HEADER = "id,name,type,price,weight,vehicle,extra"
    }
}