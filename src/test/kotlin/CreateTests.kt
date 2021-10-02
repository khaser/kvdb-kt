import kotlin.test.*
import input.*
import java.io.File

class CreateTests {
    var db = DB("$testDir/test.db", "rw")

    private fun isFilesEqual(filenameA: String, filenameB: String) {
        val dumpA = File("$testDir/$filenameA").readBytes()
        val dumpB = File("$testDir/$filenameB").readBytes()
        assert(dumpA.contentEquals(dumpB))
    }

    @BeforeTest
    fun prepare() {
        db.setLength(0)
    }

    @AfterTest
    fun cleanUp() {
        db.setLength(0)
    }

    @Test
    fun defaultConfig() {
        val emptyOptions: Options = mapOf()
        db.initDataBase(emptyOptions.toConfig())
        isFilesEqual("dbWithDefaultConfig.db", "test.db")
    }

    @Test
    fun customConfig() {
        val options: Options = mapOf(
            Option.DEFAULT_VALUE to "XXX",
            Option.PARTITIONS to "8",
            Option.VALUES_SIZE to "12"
        )
        db.initDataBase(options.toConfig())
        isFilesEqual("dbWithCustomConfig.db", "test.db")
    }
}