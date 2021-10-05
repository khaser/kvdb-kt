import input.Option
import input.Options
import DB.*
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
    fun dumpEmptyDB() {
        File("$testDir/new.db").delete()
        val options: Options = mapOf(
            Option.PARTITIONS to "15"
        )
        val db = initDataBase("$testDir/new.db", options)
        requireNotNull(db)
        assertEquals(listOf(), db.dumpAllDataBase())
        db.close()
        File("$testDir/new.db").delete()
    }

    @Test
    fun shrinkSimpleTest() {
        File("$testDir/shrinkTest.db").delete()
        File("$testDir/dumpTest.db").copyTo(File("$testDir/shrinkTest.db"))
        var db: DB? = DB("$testDir/shrinkTest.db")
        requireNotNull(db)
        val options: Options = mapOf(
            Option.PARTITIONS to "5",
            Option.KEYS_SIZE to "100",
            Option.VALUES_SIZE to "100",
            Option.DEFAULT_VALUE to db.config.defaultValue.padEnd(100)
        )
        db = db.shrink(options)
        requireNotNull(db)
        val correct = File("$testDir/correct.dump").readLines().toString().let {
            "{${it.subSequence(1, it.length - 1)}}"
        }.toSortedSet()
        assertEquals(correct, db.dumpAllDataBase().toString().toSortedSet())
        File("$testDir/shrinkTest.db").delete()
    }


    @Test
    fun shrinkEmpty() {
        File("$testDir/new.db").delete()
        var db = initDataBase("$testDir/new.db", Config())
        requireNotNull(db)
        val options: Options = mapOf(
            Option.PARTITIONS to "15",
            Option.KEYS_SIZE to "22"
        )
        db = db.shrink(options)
        requireNotNull(db)
        assertEquals(listOf(), db.dumpAllDataBase())
        File("$testDir/new.db").delete()
    }

    @Test
    fun shrinkOldDefaultTooLargeNoNewDefault() {}

    @Test
    fun shrinkOldDefaultTooLargeButNewDefaultIsGood() {}

    @Test
    fun shrinkOldDefaultAndNewDefaultTooLarge() {}

    @Test
    fun shrinkOldDefaultIsGoodButNewDefaultTooLarge() {}

    @Test
    fun shrinkAllEntriesTooLarge() { }

    @Test
    fun shrinkSomeEntriesTooLarge() { }
}
