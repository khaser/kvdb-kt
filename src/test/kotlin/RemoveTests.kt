import DB.*
import output.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.*


internal class RemoveTests {
    val envFileName = "$testDir/RemoveTests.db"
    val runFileName = "$testDir/.runRemoveTests.db"
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
    fun removeNotExistedElement() {
        val db = safeOpenDB(runFileName)
        requireNotNull(db)
        db.removeKey("Not_exist")
        assertEquals(db.config.defaultValue, db.getKeyValue("Not_exist"))
    }

    @Test
    fun removeExistedElement() {
        val db = safeOpenDB(runFileName)
        requireNotNull(db)
        db.removeKey("Exist!")
        assertEquals(db.config.defaultValue, db.getKeyValue("Exist!"))
    }


    @Test
    fun removeWithSpaces() {
        val db = safeOpenDB(runFileName)
        requireNotNull(db)
        db.removeKey(" _key ")
        assertEquals(db.config.defaultValue, db.getKeyValue(" _key "))
    }

    @Test
    fun removeLargeKey() {
        val db = safeOpenDB(runFileName)
        requireNotNull(db)
        val longKey = "longkey".padEnd(100)
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db.removeKey(longKey)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.ILLEGAL_FIELD_SIZE, "key")
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun removeDamagedFile() {
        val db = safeOpenDB(damagedFileName)
        requireNotNull(db)
        val stream = ByteArrayOutputStream().also {System.setOut(PrintStream(it))}
        db.removeKey(" _key ")
        val correctStream = ByteArrayOutputStream().also {System.setOut(PrintStream(it))}
        println(Msg.FILE_DAMAGED, damagedFileName)
        assertEquals(correctStream.toString(), stream.toString())
    }
}
