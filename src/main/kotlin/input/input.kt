package input

/** keys - strings like --help; args - string after keys */

enum class Option(val longKey: String, val shortKey: String) {
    PARTITIONS("--partitions", "-p"),
    KEYS_SIZE("--keysize", "-k"),
    VALUE_SIZE("--valuesize", "-v"),
    DEFAULT_VALUE("--default", "-e")
}

val argOptions: Set<Option> =
    setOf(Option.DEFAULT_VALUE, Option.PARTITIONS, Option.KEYS_SIZE, Option.VALUE_SIZE)

val keyShortcut = Option.values().associate { Pair(it.shortKey, it.longKey) }
val keyOption = Option.values().associateBy { it.longKey }

/** Parse all user input, main function of package*/
fun parseAllKeys(args: List<String>): Map<Option, String> {
    val result: MutableMap<Option, String> = mutableMapOf()
    //Cast all short keys to long keys
    val normalizedArgs = args.map { keyShortcut[it] ?: it }
    parseKeysWithArgs(normalizedArgs as ArrayList<String>, result)
    return result
}


/** Function for parsing options with argument like --width 100*/
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