import input.checkArgsCount
import input.getSignature
import output.Msg
import output.println
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PreparsingTests {

    val standartIn = System.`in`
    val standartOut = System.out

    @BeforeTest
    @AfterTest
    fun setUp() {
        System.setIn(standartIn)
        System.setOut(standartOut)
    }

    fun runTest(args: String, correct: List<Pair<Msg, List<String>>>, status: Action?) {
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        assertEquals(status, checkArgsCount(args.split(' ').toTypedArray()))
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        correct.forEach { println(it.first, it.second) }
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun inCorrectAction() {
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        checkArgsCount(listOf("jet", "brains").toTypedArray())
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(userManual)
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun emptyString() {
        val stream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        checkArgsCount(Array(0) {""})
        val correctStream = ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
        println(userManual)
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun setCorrect() {
        runTest("set key value file.db", listOf(), Action.SET)
    }

    @Test
    fun setNoFile() {
        runTest(
            "set",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.SET))),
            null
        )
    }

    @Test
    fun setWithOneExtraArg() {
        runTest("set key value extraarg file.db", listOf(Pair(Msg.UNUSED_KEYS, listOf("extraarg"))), Action.SET)
    }


    @Test
    fun setWithSomeExtraArgs() {
        runTest(
            "set key value more than one extra arg file.db",
            listOf(Pair(Msg.UNUSED_KEYS, listOf("more", "than", "one", "extra", "arg"))),
            Action.SET
        )
    }

    @Test
    fun setWithMissedArgs() {
        runTest(
            "set key file.db",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.SET))),
            null
        )
    }


    @Test
    fun getCorrect() {
        runTest("get key file.db", listOf(), Action.GET)
    }

    @Test
    fun getNoFile() {
        runTest(
            "get",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.GET))),
            null
        )
    }

    @Test
    fun getWithOneExtraArg() {
        runTest("get key extraarg file.db", listOf(Pair(Msg.UNUSED_KEYS, listOf("extraarg"))), Action.GET)
    }


    @Test
    fun getWithSomeExtraArgs() {
        runTest(
            "get key more than one extra arg file.db",
            listOf(Pair(Msg.UNUSED_KEYS, listOf("more", "than", "one", "extra", "arg"))),
            Action.GET
        )
    }

    @Test
    fun getWithMissedArgs() {
        runTest(
            "get file.db",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.GET))),
            null
        )
    }

    @Test
    fun removeCorrect() {
        runTest("remove key file.db", listOf(), Action.REMOVE)
    }

    @Test
    fun removeNoFile() {
        runTest(
            "remove",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.REMOVE))),
            null
        )
    }

    @Test
    fun removeWithOneExtraArg() {
        runTest("remove key extraarg file.db", listOf(Pair(Msg.UNUSED_KEYS, listOf("extraarg"))), Action.REMOVE)
    }


    @Test
    fun removeWithSomeExtraArgs() {
        runTest(
            "remove key more than one extra arg file.db",
            listOf(Pair(Msg.UNUSED_KEYS, listOf("more", "than", "one", "extra", "arg"))),
            Action.REMOVE
        )
    }

    @Test
    fun removeWithMissedArgs() {
        runTest(
            "remove file.db",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.REMOVE))),
            null
        )
    }

    @Test
    fun createCorrect() {
        runTest(
            "create file.db",
            listOf(),
            Action.CREATE
        )
    }

    @Test
    fun createCorrectWithArgs() {
        runTest(
            "create -p 10 -k 20 -d default file.db",
            listOf(),
            Action.CREATE
        )
    }

    @Test
    fun createNoFile() {
        runTest(
            "create",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.CREATE))),
            null
        )
    }

    @Test
    fun shrinkCorrect() {
        runTest(
            "shrink file.db",
            listOf(),
            Action.SHRINK
        )
    }

    @Test
    fun shrinkCorrectWithArgs() {
        runTest(
            "shrink -p 10 -k 20 -d default file.db",
            listOf(),
            Action.SHRINK

        )
    }

    @Test
    fun shrinkNoFile() {
        runTest(
            "shrink",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.SHRINK))),
            null
        )
    }

    @Test
    fun configCorrect() {
        runTest(
            "config file.db",
            listOf(),
            Action.CONFIG
        )
    }

    @Test
    fun configNoFile() {
        runTest(
            "config",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.CONFIG))),
            null
        )
    }

    @Test
    fun configExtraArgs() {
        runTest(
            "config some extra args here file.db",
            listOf(Pair(Msg.UNUSED_KEYS, listOf("some", "extra", "args", "here"))),
            Action.CONFIG
        )
    }


    @Test
    fun dumpCorrect() {
        runTest(
            "dump file.db",
            listOf(),
            Action.DUMP
        )
    }

    @Test
    fun dumpNoFile() {
        runTest(
            "dump",
            listOf(Pair(Msg.MISSED_ARGUMENTS, getSignature(Action.DUMP))),
            null
        )
    }

    @Test
    fun dumpExtraArgs() {
        runTest(
            "dump some extra args here file.db",
            listOf(Pair(Msg.UNUSED_KEYS, listOf("some", "extra", "args", "here"))),
            Action.DUMP
        )
    }
}