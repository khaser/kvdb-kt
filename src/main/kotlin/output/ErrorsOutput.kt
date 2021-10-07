package output

/** This is module to store all output messages for easy control them and reuse.*/

enum class Msg {
    UNUSED_KEYS, MISSED_ARGUMENTS, FILE_NOT_EXIST, FILE_NOT_AVAILABLE, FILE_ALREADY_EXISTS, FILE_DAMAGED, ILLEGAL_FIELD_SIZE,
    KEY_WITHOUT_ARGUMENT, OPTION_REDECLARATION, TOO_SMALL_FIELDS, TOO_LARGE_DEFAULT
}

fun println(type: Msg, args: List<String> = listOf()) {
    println(
        when (type) {
            Msg.UNUSED_KEYS -> "Warning! Skipped next keys ${args}}."
            Msg.MISSED_ARGUMENTS -> "You should enter at least '${args.joinToString(", ")}'. Use --help option to learn more."
            else -> throw IllegalArgumentException("Second argument should be a String")
        }
    )
}

fun println(type: Msg, str: String) {
    println(
        when (type) {
            Msg.FILE_NOT_AVAILABLE -> "Error! Program does not have permission to access $str."
            Msg.FILE_NOT_EXIST -> "Error! File $str does not exist."
            Msg.FILE_ALREADY_EXISTS -> "Warning! File $str already exist."
            Msg.FILE_DAMAGED -> "Error! Lost data in file $str. File was damaged."
            Msg.ILLEGAL_FIELD_SIZE -> "Error! Length of $str more than maximum allowed value."
            Msg.KEY_WITHOUT_ARGUMENT -> "Warning! After $str option must be value."
            Msg.OPTION_REDECLARATION -> "Warning! Redeclaration of option $str."
            else -> throw IllegalArgumentException("Second argument should be a List<String>")
        }
    )
}

fun println(type: Msg) {
    println(when (type) {
        Msg.TOO_SMALL_FIELDS -> "Your fields too small to contain some entries."
        Msg.TOO_LARGE_DEFAULT -> "Your fields too small to contain default. Please change shrink options."
        else -> throw IllegalArgumentException("Wrong number of arguments for this msg")
    })
}