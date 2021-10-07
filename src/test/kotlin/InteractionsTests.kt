import output.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.*

internal class InteractionsTests {

    val standartIn = System.`in`

    @BeforeTest
    fun setUp() {
        System.setIn(standartIn)
        System.setOut(PrintStream(ByteArrayOutputStream()))
    }

    fun runTest(input: String, result: Boolean) {
        System.setIn(input.byteInputStream())
        assertEquals(result, getUserAnswer(Question.OVERWRITE_FILE))
    }

    @Test
    fun yesLetterUppercase() {
        runTest("Y", true)
    }

    @Test
    fun noLetterUppercase() {
        runTest("N", false)
    }

    @Test
    fun yesLetterLowercase() {
        runTest("y", true)
    }

    @Test
    fun noLetterLowercase() {
        runTest("n", false)
    }

    @Test
    fun emptyString() {
        runTest("", false)
    }

    @Test
    fun wordBeginsFromY() {
        runTest("Yes", false)
    }

    @Test
    fun someY() {
        runTest("yY", false)
    }
}