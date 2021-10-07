import DB.initDataBase
import input.*
import output.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.*

internal class InitTests {
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
        requireNotNull(db)
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
        requireNotNull(db)
        isFilesEqual("$testDir/CreateTests/CustomConfig.db", runFileName)
    }

    @Test
    fun configWithDefaultValueOverflow() {
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "long default value",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        val stream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        val db = initDataBase(runFileName, options)
        val correctStream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        println(Msg.TOO_LARGE_DEFAULT)
        assertEquals(null, db)
        assertEquals(correctStream.toString(), stream.toString())
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
    fun createExistedFileUserAbort() {
        val stream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "XXX",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        val fileName = "$testDir/DumpTest.db"
        "n\n".byteInputStream().also { System.setIn(it) }
        val db = initDataBase(fileName, options)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.FILE_ALREADY_EXISTS, fileName)
        println(Question.OVERWRITE_FILE)
        assertEquals(null, db)
        assertEquals(correctStream.toString().trim(), stream.toString().trim())
    }

    @Test
    fun createExistedFileUserForce() {
        val stream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "XXX",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        val fileName = "$testDir/DumpTest.db"
        "Y\n".byteInputStream().also { System.setIn(it) }
        val db = initDataBase(fileName, options)
        requireNotNull(db)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.FILE_ALREADY_EXISTS, fileName)
        println(Question.OVERWRITE_FILE)
        assertEquals(correctStream.toString(), stream.toString())
    }
}