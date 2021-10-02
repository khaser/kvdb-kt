import kotlin.test.*
import java.io.File

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
        }.toSortedSet()
        assertEquals(correct, db.dumpAllDataBase(newConfig).toString().toSortedSet())
        File("$testDir/shrinkTest.db").delete()
    }

    @Test
    fun dumpEmptyDB() {
        val db = DB("$testDir/new.db", "rw")
        val config = DBConfig()
        db.initDataBase(config)
        assertEquals(mapOf(), db.dumpAllDataBase(config))
        db.close()
        File("$testDir/new.db").delete()
    }
}
