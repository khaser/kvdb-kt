import kotlin.test.*
import DB.*
import output.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal class GetTests {
    val fileName = "$testDir/GetTests.db"

    val db = saveOpenDB(fileName) ?: throw Exception("Failed create test database")

    @Test
    fun singleGetInEmptyDB() {
        assertEquals("hey", db.getKeyValue("testKey"))
    }

    @Test
    fun getNonExistedKey() {
        assertEquals(db.config.defaultValue.trim(), db.getKeyValue("does not Exist"))
    }

    @Test
    fun getLargeKey() {
        val longKey = "longkeyxxxxxxxxxxxxxxxxxxxxxxxx"
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.getKeyValue(longKey)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "key")
        assertEquals(correctStream.toString(), stream.toString())
    }
}
