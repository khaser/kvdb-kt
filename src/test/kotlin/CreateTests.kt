import DB.initDataBase
import input.*
import output.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.*

internal class CreateTests {
    private fun isFilesEqual(filenameA: String, filenameB: String) {
        val dumpA = File(filenameA).readBytes()
        val dumpB = File(filenameB).readBytes()
        assert(dumpA.contentEquals(dumpB))
    }

    val runFileName = "$testDir/.runCreateTest.db"
    val standardOut = System.out

    @BeforeTest
    fun prepare() {
        System.setOut(PrintStream(standardOut))
        File(runFileName).delete()
    }

    @AfterTest
    fun cleanUp() {
        System.setOut(PrintStream(standardOut))
        File(runFileName).delete()
    }

    @Test
    fun defaultConfig() {
        val emptyOptions: Options = mapOf()
        val db = initDataBase(runFileName, emptyOptions)
        assert(db != null)
        isFilesEqual("$testDir/CreateTests/DefaultConfig.db", runFileName)
    }

    @Test
    fun customConfig() {
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "XXX",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        val db = initDataBase(runFileName, options)
        assert(db != null)
        isFilesEqual("$testDir/CreateTests/CustomConfig.db", runFileName)
    }

    @Test
    fun createFileWithoutPermissions() {
        val stream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "XXX",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        val fileName = fileNameWithoutPermission
        val db = initDataBase(fileName, options)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.FILE_NOT_AVAILABLE, fileName)
        assertEquals(null, db)
        assertEquals(correctStream.toString().trim(), stream.toString().trim())
    }

    @Test
    fun createExistedFile() {
        val stream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "XXX",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        val fileName = "$testDir/DumpTest.db"
        val db = initDataBase(fileName, options)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.FILE_ALREADY_EXISTS, fileName)
        assertEquals(null, db)
        assertEquals(correctStream.toString().trim(), stream.toString().trim())
    }
}