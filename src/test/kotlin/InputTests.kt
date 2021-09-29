import input.Option
import input.parseAllKeys
import kotlin.test.*

internal class InputTests {

    @Test
    fun parseAllKeysNullInput() {
        val test: List<String> = listOf()
        val correct: Options = mapOf()
        assertEquals(correct, parseAllKeys(test))
    }

    fun runTest(args: String, correct: Options) {
        assertEquals(correct, parseAllKeys(args.split(' ')))
    }

    @Test
    fun parseAllKeysLongKeysOnly() {
        val correct = mapOf(
            Option.PARTITIONS to "50",
            Option.KEYS_SIZE to "30",
            Option.VALUES_SIZE to "100",
            Option.DEFAULT_VALUE to "defvalue"
        )
        runTest("--default defvalue --valuesize 100 --keysize 30 --partitions 50", correct)
    }

    @Test
    fun parseAllKeysShortKeysOnly() {
        val correct = mapOf(
            Option.PARTITIONS to "10",
            Option.KEYS_SIZE to "300",
            Option.VALUES_SIZE to "9",
            Option.DEFAULT_VALUE to "d"
        )
        runTest("-d d -v 9 -k 300 -p 10", correct)
    }

    @Test
    fun parseAllKeysMixedKeys() {
        val correct = mapOf(
            Option.VALUES_SIZE to "98",
            Option.DEFAULT_VALUE to "vimbetterthanemacs"
        )
        runTest("-d vimbetterthanemacs --valuesize 98", correct)
    }

    @Test
    fun parseAllKeysWithThash() {
        val correct = mapOf(
            Option.KEYS_SIZE to "1337"
        )
        runTest("ahaha --wrongkey -k 1337 mem -x p -p", correct)
    }
}