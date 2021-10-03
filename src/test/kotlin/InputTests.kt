import input.*
import output.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.*

internal class InputTests {

    val standardOut = System.out
    var stream =  ByteArrayOutputStream()

    @BeforeTest
    fun setUp() {
        stream = ByteArrayOutputStream().also {System.setOut(PrintStream(it))}
    }

    @AfterTest
    fun cleanUp() {
        System.setOut(standardOut)
    }

    fun runTest(args: String, correct: Options) {
        assertEquals(correct, parseUserInput(args.split(' ')))
    }

    @Test
    fun parseUserInputNullInput() {
        val test: List<String> = listOf()
        val correct: Options = mapOf()
        assertEquals(correct, parseUserInput(test))
    }

    @Test
    fun parseUserInputLongKeysOnly() {
        val correct = mapOf(
            Option.PARTITIONS to "50",
            Option.KEYS_SIZE to "30",
            Option.VALUES_SIZE to "100",
            Option.DEFAULT_VALUE to "defvalue"
        )
        runTest("--default defvalue --valuesize 100 --keysize 30 --partitions 50", correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun parseUserInputShortKeysOnly() {
        val correct = mapOf(
            Option.PARTITIONS to "10",
            Option.KEYS_SIZE to "300",
            Option.VALUES_SIZE to "9",
            Option.DEFAULT_VALUE to "d"
        )
        runTest("-d d -v 9 -k 300 -p 10", correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun parseUserInputMixedKeys() {
        val correct = mapOf(
            Option.VALUES_SIZE to "98",
            Option.DEFAULT_VALUE to "vimbetterthanemacs"
        )
        runTest("-d vimbetterthanemacs --valuesize 98", correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun parseUserInputWithWrongKey() {
        val correct = mapOf(
            Option.KEYS_SIZE to "11"
        )
        runTest("--wrongkey 1337 -k 11", correct)
        val correctStream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        println(Msg.UNUSED_KEYS, listOf("--wrongkey", "1337"))
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun parseUserInputWithMissedArgument() {
        val correct = mapOf(
            Option.KEYS_SIZE to "11"
        )
        runTest("-p -k 11", correct)
        val correctStream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        println(Msg.UNUSED_KEYS, listOf("--partitions"))
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun parseUserInputWithMissedArgumentInEnd() {
        val correct = mapOf(
            Option.KEYS_SIZE to "11"
        )
        runTest("-p -k 11 -v", correct)
        val correctStream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        println(Msg.KEY_WITHOUT_ARGUMENT, "--valuesize")
        println(Msg.UNUSED_KEYS, listOf("--partitions", "--valuesize"))
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun parseUserInputWithNonIntArgument() {
        val correct = mapOf(
            Option.KEYS_SIZE to "11",
            Option.VALUES_SIZE to "15"
        )
        runTest("-v 15 -p nonint -k 11", correct)
        val correctStream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        println(Msg.UNUSED_KEYS, listOf("--partitions", "nonint"))
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun parseUserInputArgumentWithoutKey() {
        val correct = mapOf(
            Option.PARTITIONS to "101",
            Option.DEFAULT_VALUE to "10042"
        )
        runTest("-d 10042 missedargument -p 101", correct)
        val correctStream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        println(Msg.UNUSED_KEYS, listOf("missedargument"))
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun parseUserInputWithLotOfGarbageData() {
        val correct = mapOf(
            Option.KEYS_SIZE to "1337"
        )
        runTest("ahaha --wrongkey -k 1337 mem -x p -p", correct)
        val correctStream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        println(Msg.KEY_WITHOUT_ARGUMENT, "--partitions")
        println(Msg.UNUSED_KEYS, listOf("ahaha", "--wrongkey", "mem", "-x", "p", "--partitions"))
        assertEquals(correctStream.toString(), stream.toString())
    }
}