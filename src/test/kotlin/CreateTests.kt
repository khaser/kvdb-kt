import kotlin.test.*
import input.*
import java.io.File

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
}