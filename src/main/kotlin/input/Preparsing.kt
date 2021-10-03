package input
import output.*

fun checkArgsCount(args: Array<String>): Unit? {
    if (args.size < 2) {
        println(Msg.MISSED_ARGUMENTS, listOf("mode", "DBFile")); return null
    }
    when (args[0]) {
        "get" -> {
            if (args.size < 3) {
                println(Msg.MISSED_ARGUMENTS, listOf("get", "key", "DBFile"))
                return null
            }
            if (args.size != 3) println(Msg.MISSED_ARGUMENTS, args.slice(2 until args.size - 1))
        }
        "set" -> {
            if (args.size < 4) {
                println(Msg.MISSED_ARGUMENTS, listOf("set", "key", "value", "DBFile"))
                return null
            }
            if (args.size != 4) println(Msg.MISSED_ARGUMENTS, args.slice(3 until args.size - 1))
        }
        "config", "dump" -> {
            if (args.size != 2) println(Msg.MISSED_ARGUMENTS, args.slice(1 until args.size - 1))
        }
    }
    return Unit
}