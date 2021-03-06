package input

import DB.Config
import output.*

/** keys - strings like --help; args - string after keys */

enum class Option(val longKey: String, val shortKey: String) {
    PARTITIONS("--partitions", "-p"),
    KEYS_SIZE("--keysize", "-k"),
    VALUES_SIZE("--valuesize", "-v"),
    DEFAULT_VALUE("--default", "-d")
}

typealias Options = Map<Option, String>

val optionsWithIntegerArg = setOf(Option.PARTITIONS, Option.KEYS_SIZE, Option.VALUES_SIZE)
val keyShortcut = Option.values().associate { Pair(it.shortKey, it.longKey) }
val keyOption = Option.values().associateBy { it.longKey }

/** Parse all user input, main function of package */
fun parseUserInput(args: List<String>): Options {
    //Cast all short keys to long keys
    val normalizedArgs = args.map { keyShortcut[it] ?: it }
    return parseKeysWithArgs(normalizedArgs as MutableList<String>)
}


/** Function for parsing options with argument like --partitions 100 */
private fun parseKeysWithArgs(args: MutableList<String>): MutableMap<Option, String> {
    val result: MutableMap<Option, String> = mutableMapOf()
    val dropped: MutableList<String> = mutableListOf()
    while (args.isNotEmpty()) {
        dropped.addAll(args.takeWhile { !keyOption.containsKey(it) }
            .also { repeat(it.size) { args.removeFirst() } })
        if (args.isEmpty()) break
        if (args.size == 1) {
            println(Msg.KEY_WITHOUT_ARGUMENT, args[0])
            dropped.add(args[0])
            break
        }
        val option = keyOption[args[0]]
        requireNotNull(option)
        if (optionsWithIntegerArg.contains(option) && args[1].toIntOrNull() == null) {
            dropped.add(args.removeFirst())
            continue
        }
        if (result.containsKey(option)) println(Msg.OPTION_REDECLARATION, args[0])
        result[option] = args[1]
        repeat(2) { args.removeFirst() }
    }
    if (dropped.isNotEmpty()) println(Msg.UNUSED_KEYS, dropped)
    return result
}