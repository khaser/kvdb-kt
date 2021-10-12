import kotlin.test.*
import DB.*
import output.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal class GetTests {
    val fileName = "$testDir/GetTests.db"

    val db = safeOpenDB(fileName) ?: throw Exception("Failed create test database")

    @Test
    fun singleGet() {
        assertEquals("hey", db.getKeyValue("testKey"))
    }

    @Test
    fun singleGetWithSpaces() {
        assertEquals("you", db.getKeyValue("  key with spaces  "))
    }

    @Test
    fun getNonExistedKey() {
        assertEquals(db.config.defaultValue, db.getKeyValue("does not Exist"))
    }

    @Test
    fun getLargeKey() {
        val longKey = "longkey".padEnd(100)
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.getKeyValue(longKey)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "key")
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun getInDamagedFile() {
        val db = safeOpenDB(damagedFileName) ?: throw Exception("Failed create test database")
        val stream = ByteArrayOutputStream().also {System.setOut(PrintStream(it))}
        db.getKeyValue("it should fail")
        val correctStream = ByteArrayOutputStream().also {System.setOut(PrintStream(it))}
        println(Msg.FILE_DAMAGED, damagedFileName)
        assertEquals(correctStream.toString(), stream.toString())
    }
}
