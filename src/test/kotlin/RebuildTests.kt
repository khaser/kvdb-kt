import input.Option
import input.Options
import kotlin.test.*
import java.io.File

internal class RebuildTests {

    @Test
    fun dumpTest() {
        val db = DB("$testDir/dumpTest.db")
        val correct = File("$testDir/correct.dump").readLines().toString().let {
            "{${it.subSequence(1, it.length - 1)}}"
        }
        assertEquals(correct, db.dumpAllDataBase().toString())
    }

    @Test
    fun rebuildTest() {
        File("$testDir/shrinkTest.db").delete()
        File("$testDir/dumpTest.db").copyTo(File("$testDir/shrinkTest.db"))
        var db = DB("$testDir/shrinkTest.db")
        val options: Options = mapOf(
            Option.PARTITIONS to "5",
            Option.KEYS_SIZE to "100",
            Option.VALUES_SIZE to "100",
            Option.DEFAULT_VALUE to db.config!!.defaultValue.padEnd(100)
        )
        db = db.shrink(options)
        val correct = File("$testDir/correct.dump").readLines().toString().let {
            "{${it.subSequence(1, it.length - 1)}}"
        }.toSortedSet()
        assertEquals(correct, db.dumpAllDataBase().toString().toSortedSet())
        File("$testDir/shrinkTest.db").delete()
    }

    @Test
    fun dumpEmptyDB() {
        val options: Options = mapOf(
            Option.PARTITIONS to "15"
        )
        val db = initDataBase("$testDir/new.db", options)
        requireNotNull(db)
        assertEquals(mapOf(), db.dumpAllDataBase())
        db.close()
        File("$testDir/new.db").delete()
    }
}
