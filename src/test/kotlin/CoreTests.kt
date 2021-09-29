import input.Option
import input.toConfig
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

val testDir = "testFiles"

fun isFilesEqual(filenameA: String, filenameB: String) {
    val dumpA = File("$testDir/$filenameA").readBytes()
    val dumpB = File("$testDir/$filenameB").readBytes()
    assert(dumpA.contentEquals(dumpB))
}


internal class TestCreate {

    var db = DB("$testDir/test.db", "rw")

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

internal class SetGetTests {

    val db = DB("$testDir/setGetTests.db", "rw")

    @Test
    fun getAfterSet() {
        val config = DBConfig(3, 11, 15, "HEH".padEnd(15))
        db.initDataBase(config)
        val requests = mapOf(
            Pair("some", "my"),
            Pair("words", "programming"),
            Pair("for", "the"),
            Pair("testing", "best"),
            Pair("my", "my"),
            Pair("kvdb", "some"),
            Pair("project.", "words"),
            Pair("Vim", "project."),
            Pair("the", "Kotlin"),
            Pair("best", "Vim"),
            Pair("text", "the"),
            Pair("editor.", "language"),
            Pair("Kotlin", "editor."),
            Pair("the", "the"),
            Pair("best", "my"),
            Pair("programming", "project."),
            Pair("language", "the"),
        )
        requests.forEach {
            db.setKeyValue(config, it.key, it.value)
        }
        requests.forEach {
            assertEquals(it.value, db.getKeyValue(config, it.key))
        }
        assertEquals(config.defaultValue.trim(), db.getKeyValue(config, "keyNoValue"))
    }

    @Test
    fun stressTest() {
        mixedGetSet(DBConfig(1000, 25, 25, "DEFAULT".padEnd(25)))
        mixedGetSet(DBConfig(100, 25, 25, "ANOTHER DEFAULT".padEnd(25)))
    }

    fun mixedGetSet(config: DBConfig) {
        db.initDataBase(config)

        val solver: MutableMap<String, String> = mutableMapOf()

        File("$testDir/Query5000").readLines().forEach {
            val args = it.split(' ')
            when (args[0]) {
                "set" -> {
                    solver[args[1]] = args[2]
                    db.setKeyValue(config, args[1], args[2])
                }
                "get" -> {
                    assertEquals(
                        solver.getOrDefault(args[1], config.defaultValue).trim(),
                        db.getKeyValue(config, args[1]).trim()
                    )
                }
            }
        }
    }

}

internal class RebuildTests {

    @Test
    fun dumpTest() {
        val db = DB("$testDir/dumpTest.db", "rw")
        val config = db.readConfig()
        val correct = File("$testDir/correct.dump").readLines().toString().let {
                "{${it.subSequence(1, it.length - 1)}}"
            }
        assertEquals(correct, db.dumpAllDataBase(config).toString())
    }

    @Test
    fun rebuildTest() {
        File("$testDir/shrinkTest.db").delete()
        File("$testDir/dumpTest.db").copyTo(File("$testDir/shrinkTest.db"))
        val db = DB("$testDir/shrinkTest.db", "rw")
        val config = db.readConfig()
        val newConfig = DBConfig(5,100, 100, config.defaultValue.padEnd(100))
        db.shrink(config, newConfig)
        val correct = File("$testDir/correct.dump").readLines().toString().let {
            "{${it.subSequence(1, it.length - 1)}}"
        }
        assertEquals(correct, db.dumpAllDataBase(newConfig).toString())
        File("$testDir/shrinkTest.db").delete()
    }
}