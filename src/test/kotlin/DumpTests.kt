import input.*
import DB.*
import output.*
import kotlin.test.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

internal class DumpTests {

    val standardOut = System.out
    val runFileName = "$testDir/.runDumpTest.db"

    @BeforeTest
    @AfterTest
    fun setUp() {
        File(runFileName).delete()
        System.setOut(standardOut)
    }

    @Test
    fun entriesWithSpaces() {
        File("$testDir/DumpTest.db").copyTo(File(runFileName))
        val db = saveOpenDB(runFileName)
        requireNotNull(db)
        val correct = File("$testDir/Correct.dump").readLines()
        assertEquals(correct, db.dumpAllDataBase()?.map{"${it.first}, ${it.second}"})
    }

    @Test
    fun emptyDB() {
        val options: Options = mapOf(
            Option.PARTITIONS to "15"
        )
        val db = initDataBase(runFileName, options)
        requireNotNull(db)
        assertEquals(listOf(), db.dumpAllDataBase())
        db.close()
    }

    @Test
    fun damagedDB() {
        val fileName = damagedFileName
        val db = saveOpenDB(fileName)
        requireNotNull(db)
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        assertEquals(null, db.dumpAllDataBase())
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(Msg.FILE_DAMAGED, fileName)
        assertEquals(correctStream.toString(), stream.toString())
    }
}
