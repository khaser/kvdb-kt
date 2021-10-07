import DB.*
import input.*
import output.*
import java.io.*
import kotlin.test.*

internal class ShrinkTests {

    val standardIn = System.`in`
    val standartOut = System.out
    val envFileName = "$testDir/ShrinkTest.db"
    val runFileName = "$testDir/.runShrinkTest.db"

    @BeforeTest
    @AfterTest
    fun streamsSetUp() {
        System.setIn(standardIn)
        System.setOut(standartOut)
    }

    @BeforeTest
    fun setUp() {
        File(runFileName).delete()
        File(envFileName).copyTo(File(runFileName), true)
    }

    @AfterTest
    fun cleanUp() {
        File(runFileName).delete()
    }

    @Test
    fun simpleTest() {
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
        assertEquals(correct, db.dumpAllDataBase()?.map{"${it.first}, ${it.second}"}?.toSortedSet())
    }


    @Test
    fun emptyDB() {
        File(runFileName).delete()
        var db = initDataBase(runFileName, Config())
        requireNotNull(db)
        val options: Options = mapOf(
            Option.PARTITIONS to "15",
            Option.KEYS_SIZE to "22"
        )
        db = db.shrink(options)
        requireNotNull(db)
        assertEquals(listOf(), db.dumpAllDataBase())
    }

    @Test
    fun oldDefaultTooLargeNoNewDefault() {
        var db = saveOpenDB(runFileName)
        requireNotNull(db)

        db = db.shrink(mapOf(Option.DEFAULT_VALUE to db.config.defaultValue.padEnd(db.config.valueSize)))
        requireNotNull(db)
        val options: Options = mapOf(
            Option.PARTITIONS to "3",
            Option.VALUES_SIZE to "24",
        )
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db = db.shrink(options)
        assertEquals(null, db)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.TOO_LARGE_DEFAULT)
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun oldDefaultTooLargeButNewDefaultIsGood() {
        var db = saveOpenDB(runFileName)
        requireNotNull(db)

        db = db.shrink(mapOf(Option.DEFAULT_VALUE to db.config.defaultValue.padEnd(db.config.valueSize)))
        requireNotNull(db)
        val options: Options = mapOf(
            Option.PARTITIONS to "3",
            Option.VALUES_SIZE to "24",
            Option.DEFAULT_VALUE to "khaser"
        )
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db = db.shrink(options)
        requireNotNull(db)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun oldDefaultAndNewDefaultTooLarge() {
        var db = saveOpenDB(runFileName)
        requireNotNull(db)

        db = db.shrink(mapOf(Option.DEFAULT_VALUE to db.config.defaultValue.padEnd(db.config.valueSize)))
        requireNotNull(db)
        val options: Options = mapOf(
            Option.PARTITIONS to "3",
            Option.VALUES_SIZE to "24",
            Option.DEFAULT_VALUE to "long default".padEnd(25)
        )
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        db = db.shrink(options)
        assertEquals(null, db)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.TOO_LARGE_DEFAULT)
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun oldDefaultIsGoodButNewDefaultTooLargeUserAgree() {
        var db = saveOpenDB(runFileName)
        requireNotNull(db)

        val options: Options = mapOf(
            Option.PARTITIONS to "3",
            Option.VALUES_SIZE to "24",
            Option.DEFAULT_VALUE to "khaser".padEnd(25)
        )
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        "Y\n".byteInputStream().also { System.setIn(it) }
        db = db.shrink(options)
        requireNotNull(db)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.TOO_LARGE_DEFAULT)
        println(Question.USE_OLD_DEFAULT)
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun oldDefaultIsGoodButNewDefaultTooLargeUserAbourt() {
        var db = saveOpenDB(runFileName)
        requireNotNull(db)

        val options: Options = mapOf(
            Option.PARTITIONS to "3",
            Option.VALUES_SIZE to "24",
            Option.DEFAULT_VALUE to "khaser".padEnd(25)
        )
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        "n\n".byteInputStream().also { System.setIn(it) }
        db = db.shrink(options)
        assertEquals(null, db)
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.TOO_LARGE_DEFAULT)
        println(Question.USE_OLD_DEFAULT)
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun allEntriesTooLarge() { }

    @Test
    fun someEntriesTooLarge() { }
}