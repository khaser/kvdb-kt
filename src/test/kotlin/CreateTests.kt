import kotlin.test.*
import input.*
import output.*
import DB.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class CreateTests {
    private fun isFilesEqual(filenameA: String, filenameB: String) {
        val dumpA = File("$testDir/$filenameA").readBytes()
        val dumpB = File("$testDir/$filenameB").readBytes()
        assert(dumpA.contentEquals(dumpB))
    }

    val fileName = "$testDir/test.db"

    @BeforeTest
    fun prepare() {
        File(fileName).delete()
    }

    @AfterTest
    fun cleanUp() {
        File(fileName).delete()
    }

    @Test
    fun defaultConfig() {
        val emptyOptions: Options = mapOf()
        val db = initDataBase(fileName, emptyOptions)
        isFilesEqual("dbWithDefaultConfig.db", "test.db")
    }

    @Test
    fun customConfig() {
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "XXX",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        val db = initDataBase(fileName, options)
        isFilesEqual("dbWithCustomConfig.db", "test.db")
    }

    @Test
    fun createOnFileWithoutPermissions() {
        val standardOut = System.out
        val stream = ByteArrayOutputStream()
        System.setOut(PrintStream(stream))
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "XXX",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        val fileName = "$testDir/fileWithoutPermission.db"
        val db = initDataBase(fileName, options)
        val correctStream = ByteArrayOutputStream()
        System.setOut(PrintStream(correctStream))
        println(Msg.FILE_NOT_AVAILABLE, fileName)
        assertEquals(correctStream.toString().trim(), stream.toString().trim())
        System.setOut(standardOut)
    }

    @Test
    fun createOnExistedFile() {
        val standardOut = System.out
        val stream = ByteArrayOutputStream()
        System.setOut(PrintStream(stream))
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "XXX",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        val fileName = "$testDir/dumpTest.db"
        val db = initDataBase(fileName, options)
        val correctStream = ByteArrayOutputStream()
        System.setOut(PrintStream(correctStream))
        println(Msg.FILE_ALREADY_EXISTS, fileName)
        assertEquals(correctStream.toString().trim(), stream.toString().trim())
        System.setOut(standardOut)
    }
}