import java.io.File
import kotlin.test.*

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

    private fun mixedGetSet(config: DBConfig) {
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
