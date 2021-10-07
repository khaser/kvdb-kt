import java.io.File
import kotlin.test.*
import DB.*

internal class StressTests {

    val fileName = "$testDir/IntegrityTests.db"

    @BeforeTest
    fun prepare() {
        File(fileName).delete()
    }

    @AfterTest
    fun cleanUp() {
        File(fileName).delete()
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
