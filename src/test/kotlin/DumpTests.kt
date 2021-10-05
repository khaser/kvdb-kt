import input.Option
import input.Options
import DB.*
import kotlin.test.*
import java.io.File

internal class DumpTests {

    @Test
    fun entriesWithSpaces() {
        val db = saveOpenDB("$testDir/DumpTest.db")
        requireNotNull(db)
        val correct = File("$testDir/Correct.dump").readLines()
        assertEquals(correct, db.dumpAllDataBase().map{"${it.first}, ${it.second}"})
    }

    @Test
    fun emptyDB() {
        val fileName = "$testDir/.runDumpTest.db"
        File(fileName).delete()
        val options: Options = mapOf(
            Option.PARTITIONS to "15"
        )
        val db = initDataBase(fileName, options)
        requireNotNull(db)
        assertEquals(listOf(), db.dumpAllDataBase())
        db.close()
        File(fileName).delete()
    }
}
