import java.io.File
import kotlin.test.*
import DB.*
import output.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal class RemoveTests {
    val standartOut = System.out
    val fileOriginal = "$testDir/RemoveTests.db"
    val fileName = "$testDir/new.db"
    val config = Config(2, 10, 10, "default")

    @BeforeTest
    fun prepare() {
        File(fileOriginal).copyTo(File(fileName), true)
    }

    @AfterTest
    fun cleanUp() {
        File(fileName).delete()
        System.setOut(standartOut)
    }

    @Test
    fun singleRemove() {
        val db = saveOpenDB(fileName)
        requireNotNull(db)
        assertEquals("hey", db.getKeyValue("testKey"))
        db.removeKey("testKey")
        assertEquals(db.config.defaultValue.trim(), db.getKeyValue("testKey"))
    }

    @Test
    fun removeNonExisted() {
        val db = saveOpenDB(fileName)
        requireNotNull(db)
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.removeKey("nonExistedKey")
        assertEquals("", stream.toString())
        assertEquals(db.config.defaultValue.trim(), db.getKeyValue("nonExistedKey"))
    }

    @Test
    fun removeTwice() {
        val db = saveOpenDB(fileName)
        requireNotNull(db)
        assertEquals("andrew", db.getKeyValue("khaser"))
        db.removeKey("khaser")
        db.removeKey("khaser")
        assertEquals(config.defaultValue.trim(), db.getKeyValue("khaser"))
    }

    @Test
    fun removeLargeKey() {
        val db = saveOpenDB(fileName)
        requireNotNull(db)
        val longKey = "longkeyxxxxxxxxxxxxxxxxxxxxxxxx"
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.removeKey(longKey)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "key")
        assertEquals(correctStream.toString(), stream.toString())
    }
}
