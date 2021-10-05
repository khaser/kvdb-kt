import DB.saveOpenDB
import output.Msg
import output.println
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


internal class SetTests {
    val envFileName = "$testDir/SetTests.db"
    val runFileName = "$testDir/.runSetTests.db"
    val sysOut = System.out

    @BeforeTest
    fun prepare() {
        File(envFileName).copyTo(File(runFileName), true)
    }

    @AfterTest
    fun cleanUp() {
        File(runFileName).delete()
        System.setOut(sysOut)
    }

    @Test
    fun singleSetNewElement() {
        val db = saveOpenDB(runFileName)
        requireNotNull(db)
        db.setKeyValue("hey", "you")
        assertEquals("you", db.getKeyValue("hey"))
    }

    @Test
    fun emptyKey() {
        val db = saveOpenDB(runFileName)
        requireNotNull(db)
        db.setKeyValue("", "val for emp key")
        assertEquals("val for emp key", db.getKeyValue(""))
        //Check saving default value for all partitions
        assertEquals(db.config.defaultValue, db.getKeyValue("void key"))
        assertEquals(db.config.defaultValue, db.getKeyValue("void key2"))
    }

    @Test
    fun singleSetExistedElement() {
        val db = saveOpenDB(runFileName)
        requireNotNull(db)
        db.setKeyValue("hello", "stranger")
        assertEquals("stranger", db.getKeyValue("hello"))
    }

    @Test
    fun singleSetWithSpaces() {
        val db = saveOpenDB(runFileName)
        requireNotNull(db)
        db.setKeyValue(" _key ", " __value__ ")
        assertEquals(" __value__ ", db.getKeyValue(" _key "))
    }

    @Test
    fun setLargeKey() {
        val db = saveOpenDB(runFileName)
        requireNotNull(db)
        val longKey = "longkey".padEnd(100)
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.setKeyValue(longKey, "val")
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "key")
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun setLargeValue() {
        val db = saveOpenDB(runFileName)
        requireNotNull(db)
        val longValue = "longvalue".padEnd(100)
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.setKeyValue("testKey", longValue)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "value")
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun setLargeKeyAndValue() {
        val db = saveOpenDB(runFileName)
        requireNotNull(db)
        val longKey = "longkey".padEnd(100)
        val longValue = "longvalue".padEnd(100)
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.setKeyValue(longKey, longValue)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "key")
        println(Msg.ILLEGAL_FIELD_SIZE, "value")
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun setDamagedFile() {
        val db = saveOpenDB(damagedFileName)
        requireNotNull(db)
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.setKeyValue(" _key ", " __value__ ")
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        repeat(2) { println(Msg.FILE_DAMAGED, damagedFileName) }
        assertEquals(correctStream.toString(), stream.toString())
    }
}
