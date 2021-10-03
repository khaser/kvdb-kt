import java.io.File
import kotlin.test.*
import DB.*
import output.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal class SetTests {
    val fileName = "$testDir/SetTests.db"
    val config = Config(2, 10, 10, "default")

    @BeforeTest
    fun prepare() {
        File(fileName).delete()
    }

    @AfterTest
    fun cleanUp() {
        File(fileName).delete()
    }

    @Test
    fun singleSetInEmptyDB() {
        val db = initDataBase(fileName, config)
        requireNotNull(db)
        db.setKeyValue("testKey", "hey")
        assertEquals("hey", db.getKeyValue("testKey"))
    }

    @Test
    fun changeSetInEmptyDB() {
        val db = initDataBase(fileName, config)
        requireNotNull(db)
        db.setKeyValue("testKey", "hey")
        db.setKeyValue("testKey", "heyhey")
        assertEquals("heyhey", db.getKeyValue("testKey"))
    }

    @Test
    fun setToDefaultDB() {
        val db = initDataBase(fileName, config)
        requireNotNull(db)
        db.setKeyValue("default", config.defaultValue)
        assertEquals(config.defaultValue.trim(), db.getKeyValue("default"))
    }

    @Test
    fun setLargeKey() {
        val db = initDataBase(fileName, config)
        requireNotNull(db)
        val longKey = "longkeyxxxxxxxxxxxxxxxxxxxxxxxx"
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.setKeyValue(longKey, "val")
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "key")
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun setLargeValue() {
        val db = initDataBase(fileName, config)
        requireNotNull(db)
        val longValue = "longvaluexxxxxxxxxxxxxxxxxxxxxxxx"
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.setKeyValue("testKey", longValue)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "value")
        assertEquals(correctStream.toString(), stream.toString())
    }


    @Test
    fun setLargeKeyAndValue() {
        val db = initDataBase(fileName, config)
        requireNotNull(db)
        val longKey = "longkeyxxxxxxxxxxxxxxxxxxxxxxxx"
        val longValue = "longvaluexxxxxxxxxxxxxxxxxxxxxxxx"
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.setKeyValue(longKey, longValue)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "key")
        println(Msg.ILLEGAL_FIELD_SIZE, "value")
        assertEquals(correctStream.toString(), stream.toString())
    }
}
