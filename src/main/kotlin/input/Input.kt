package input

import DBConfig
import Options
import kotlin.system.exitProcess

/** keys - strings like --help; args - string after keys */

enum class Option(val longKey: String, val shortKey: String) {
    PARTITIONS("--partitions", "-p"),
    KEYS_SIZE("--keysize", "-k"),
    VALUES_SIZE("--valuesize", "-v"),
    DEFAULT_VALUE("--default", "-d")
}

val argOptions: Set<Option> =
    setOf(Option.DEFAULT_VALUE, Option.PARTITIONS, Option.KEYS_SIZE, Option.VALUES_SIZE)

val keyShortcut = Option.values().associate { Pair(it.shortKey, it.longKey) }
val keyOption = Option.values().associateBy { it.longKey }

/** Parse all user input, main function of package */
fun parseAllKeys(args: List<String>): Options {
    val result: MutableMap<Option, String> = mutableMapOf()
    //Cast all short keys to long keys
    val normalizedArgs = args.map { keyShortcut[it] ?: it }
    parseKeysWithArgs(normalizedArgs as ArrayList<String>, result)
    return result
}


/** Function for parsing options with argument like --width 100 */
private fun parseKeysWithArgs(args: ArrayList<String>, result: MutableMap<Option, String>) {
    val dropped: MutableList<String> = mutableListOf()
    while (args.isNotEmpty()) {
        dropped.addAll(args.takeWhile { !argOptions.contains(keyOption[it]) }
            .also { repeat(it.size) { args.removeFirst() } })
        if (args.isEmpty()) break
        if (args.size == 1) {
            println("Warning!!! After ${args[0]} option must be value")
            dropped.add(args[0])
            break
        }
        with(keyOption[args[0]]!!) {
            if (result.containsKey(this)) {
                println("Warning!!! Redeclaration of option ${args[0]}")
            }
            result[this] = args[1]
            repeat(2) { args.removeFirst() }
        }
    }
    if (dropped.isNotEmpty()) println("Was ignored next keys: ${dropped.joinToString(" ")}")
}

fun Options.toConfig(): DBConfig {
    val partitions = this[Option.PARTITIONS]?.toInt() ?: 5000
    val keySize = this[Option.KEYS_SIZE]?.toInt() ?: 255
    val valueSize = this[Option.VALUES_SIZE]?.toInt() ?: 255
    val defaultValue = (this[Option.DEFAULT_VALUE] ?: "").padEnd(valueSize)
    if (defaultValue.length != valueSize) {
        println("Aborting. Default value length must not be more, than value size.")
        exitProcess(0)
    }
    return DBConfig(partitions, keySize, valueSize, defaultValue)
}