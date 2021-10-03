import java.io.File
import kotlin.test.*
import DB.*

internal class SetGetTests {

    val fileName = "$testDir/setGetTests.db"

    @BeforeTest
    fun prepare() {
        File(fileName).delete()
    }

    @AfterTest
    fun cleanUp() {
        File(fileName).delete()
    }

    @Test
    fun getAfterSet() {
        val config = Config(3, 11, 15, "HEH".padEnd(15))
        val db = initDataBase("$testDir/setGetTests.db", config)
        requireNotNull(db)
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
            db.setKeyValue(it.key, it.value)
        }
        requests.forEach {
            assertEquals(it.value, db.getKeyValue(it.key))
        }
        assertEquals(config.defaultValue.trim(), db.getKeyValue("keyNoValue"))
    }

    @Test
    fun stressTest1000Partitions() {
        mixedGetSet(Config(1000, 25, 25, "DEFAULT".padEnd(25)))
    }

    @Test
    fun stressTest100Partitions() {
        mixedGetSet(Config(100, 25, 25, "ANOTHER DEFAULT".padEnd(25)))
    }

    @Test
    fun stressTest10Partitions() {
        mixedGetSet(Config(10, 25, 25, "one more default".padEnd(25)))
    }

    private fun mixedGetSet(config: Config) {
        val db = initDataBase(fileName, config)
        requireNotNull(db)
        val solver: MutableMap<String, String> = mutableMapOf()
        File("$testDir/Query5000").readLines().forEach {
            val args = it.split(' ')
            when (args[0]) {
                "set" -> {
                    solver[args[1]] = args[2]
                    db.setKeyValue(args[1], args[2])
                }
                "get" -> {
                    assertEquals(
                        solver.getOrDefault(args[1], config.defaultValue).trim(),
                        db.getKeyValue(args[1])?.trim()
                    )
                }
            }
        }
    }
}
