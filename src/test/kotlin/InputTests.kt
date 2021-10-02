import input.*
import kotlin.test.*

internal class InputTests {

    @Test
    fun parseUserInputNullInput() {
        val test: List<String> = listOf()
        val correct: Options = mapOf()
        assertEquals(correct, parseUserInput(test))
    }

    fun runTest(args: String, correct: Options) {
        assertEquals(correct, parseUserInput(args.split(' ')))
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
    }

    @Test
    fun parseUserInputMixedKeys() {
        val correct = mapOf(
            Option.VALUES_SIZE to "98",
            Option.DEFAULT_VALUE to "vimbetterthanemacs"
        )
        runTest("-d vimbetterthanemacs --valuesize 98", correct)
    }

    @Test
    fun parseUserInputWithWrongKey() {
        val correct = mapOf(
            Option.KEYS_SIZE to "11"
        )
        runTest("--wrongkey 1337 -k 11", correct)
    }

    @Test
    fun parseUserInputWithMissedArgument() {
        val correct = mapOf(
            Option.KEYS_SIZE to "11"
        )
        runTest("-p -k 11", correct)
    }

    @Test
    fun parseUserInputWithNonIntArgument() {
        val correct = mapOf(
            Option.KEYS_SIZE to "11",
            Option.VALUES_SIZE to "15"
        )
        runTest("-v 15 -p nonint -k 11", correct)
    }

    @Test
    fun parseUserInputArgumentWithoutKey() {
        val correct = mapOf(
            Option.PARTITIONS to "101",
            Option.DEFAULT_VALUE to "10042"
        )
        runTest("-d 10042 missedargument -p 101", correct)
    }

    @Test
    fun parseUserInputWithLotOfGarbageData() {
        val correct = mapOf(
            Option.KEYS_SIZE to "1337"
        )
        runTest("ahaha --wrongkey -k 1337 mem -x p -p", correct)
    }
}