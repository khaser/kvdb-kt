package input
import userManual
import output.*
import Action

/** Return list of must have arguments for action*/
fun getSignature(mode: Action): List<String> {
    return when (mode) {
        Action.GET, Action.REMOVE -> listOf(mode.toString(), "key", "DBFile")
        else -> listOf("mode", "DBFile")
    }
}

/** Primary check correction of arguments. Return Action if success. */
fun checkArgsCount(args: Array<String>): Action? {
    val mode = try {
        Action.valueOf(args[0].uppercase())
    } catch (error: Exception) {
        println(userManual)
        return null
    }
    if (args.size < 2) {
        println(Msg.MISSED_ARGUMENTS, getSignature(mode)); return null
    }
    when (mode) {
        Action.GET, Action.REMOVE -> {
            if (args.size < 3) {
                println(Msg.MISSED_ARGUMENTS, getSignature(mode))
                return null
            }
            if (args.size != 3) println(Msg.UNUSED_KEYS, args.slice(2 until args.size - 1))
        }
        Action.SET -> {
            if (args.size < 4) {
                println(Msg.MISSED_ARGUMENTS, getSignature(mode))
                return null
            }
            if (args.size != 4) println(Msg.UNUSED_KEYS, args.slice(3 until args.size - 1))
        }
        Action.CONFIG, Action.DUMP -> {
            if (args.size != 2) println(Msg.UNUSED_KEYS, args.slice(1 until args.size - 1))
        }
    }
    return mode
}