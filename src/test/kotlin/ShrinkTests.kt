import DB.*
import input.*
import java.io.File
import kotlin.test.*

internal class ShrinkTests {

    val runFileName = "$testDir/.runShrinkTest"

    @BeforeTest
    fun setUp() {
        File(runFileName).delete()
    }

    @AfterTest
    fun cleanUp() {
        File(runFileName).delete()
    }

    @Test
    fun simpleTest() {
        val fileName = "$testDir/ShrinkTest.db"
        File(fileName).copyTo(File(runFileName), true)
        var db: DB? = saveOpenDB(runFileName)
        requireNotNull(db)
        val options: Options = mapOf(
            Option.PARTITIONS to "5",
            Option.KEYS_SIZE to "50",
            Option.VALUES_SIZE to "100",
        )
        db = db.shrink(options)
        requireNotNull(db)
        val correct = File("$testDir/Correct.dump").readLines().toSortedSet()
        assertEquals(correct, db.dumpAllDataBase().map{"${it.first}, ${it.second}"}.toSortedSet())
    }


    @Test
    fun emptyDB() {
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