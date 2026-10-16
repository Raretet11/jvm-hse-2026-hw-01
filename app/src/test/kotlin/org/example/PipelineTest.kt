package org.example

import org.example.export.ExportFormat
import org.example.normalizer.ProductNormalizerImpl
import org.example.parser.ProductParserImpl
import org.example.reader.CsvReader
import org.example.validator.ProductValidatorImpl
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PipelineTest {
    @TempDir
    lateinit var tempDir: Path

    @Test fun reportsStatistics() {
        val output = runPipeline()

        assertTrue(output.contains("Обработано строк: 12"))
        assertTrue(output.contains("Успешно загружено товаров: 6"))
        assertTrue(output.contains("Ошибок парсинга: 3"))
        assertTrue(output.contains("Ошибок валидации: 3"))
    }

    @Test fun exportsOnlyValidProducts() {
        runPipeline()

        val report = tempDir.resolve("output.txt").toFile().readText(Charsets.UTF_8)

        assertEquals(6, Regex("""\(id=\d+\)""").findAll(report).count())
    }

    @Test fun reportsParseErrorsWithSourceLineNumbers() {
        val output = runPipeline()

        assertTrue(output.contains("[ОШИБКА] Строка 7: Не удалось распарсить цену: 'abc'"))
        assertTrue(output.contains("[ОШИБКА] Строка 11: volume должно быть числом"))
        assertTrue(output.contains("[ОШИБКА] Строка 12: Неизвестный тип товара: 'battery'"))
    }

    @Test fun reportsValidationErrorsWithSourceLineNumbers() {
        val output = runPipeline()

        assertTrue(output.contains("[ОШИБКА] Строка 8: Пустое название товара"))
        assertTrue(output.contains("[ОШИБКА] Строка 9: Недопустимый цвет: 'red'"))
        assertTrue(output.contains("[ОШИБКА] Строка 10: Цена должна быть положительной"))
    }

    private fun runPipeline(): String {
        val input = tempDir.resolve("input.csv")
        input.toFile().writeText(CSV, Charsets.UTF_8)

        val output = tempDir.resolve("output.txt")
        return captureOutput {
            Pipeline(
                reader = CsvReader(input.toString()),
                exportFilename = output.toString(),
                exportFormat = ExportFormat.TXT,
                parser = ProductParserImpl,
                validator = ProductValidatorImpl,
                normalizer = ProductNormalizerImpl,
            ).run()
        }
    }

    private fun captureOutput(action: () -> Unit): String {
        val buffer = ByteArrayOutputStream()
        val original = System.out
        System.setOut(PrintStream(buffer, true, Charsets.UTF_8))
        try {
            action()
        } finally {
            System.setOut(original)
        }
        return buffer.toString(Charsets.UTF_8)
    }

    private companion object {
        val CSV = """
            id,name,type,price,weight,vehicle,extra
            1,"Mobil 1, Synthetic",oil,${'$'}12.5,10lb,PASSENGER,viscosity=5W-30;volume=4
            2,Castrol Magnatec,oil,300.5руб,1.2kg,PASSENGER,viscosity=10W-40;volume=1
            3,"Michelin Primacy",tire,15${'$'},0.3kg,PASSENGER,diameter=16;season=SUMMER
            4,Nokian Hakkapeliitta,tire,25руб,2lb,TRUCK,diameter=18;season=WINTER
            5,Щетки стеклоочистителя,accessory,100руб,0.1kg,PASSENGER,material=резина;color=черный
            6,"Bosch, Air Filter",accessory,${'$'}7.5,0.05kg,MOTORCYCLE,material=бумага;color=белый
            7,Масло,oil,abc,0.5kg,PASSENGER,viscosity=5W-30
            8,,tire,10${'$'},0.2kg,PASSENGER,diameter=16
            9,Присадка,accessory,50руб,0.1kg,PASSENGER,material=пластик;color=red
            10,Шина,tire,-5${'$'},0.3kg,PASSENGER,diameter=17
            11,Масло,oil,100руб,0.5kg,PASSENGER,viscosity=5W-30;volume=abc
            12,Аккумулятор,battery,200руб,0.4kg,PASSENGER,material=свинец
        """.trimIndent() + "\n"
    }
}