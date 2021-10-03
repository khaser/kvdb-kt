enum class Msg {
    UNUSED_KEYS, MISSED_ARGUMENTS, FILE_NOT_EXIST, FILE_NOT_AVAILABLE, FILE_ALREADY_EXISTS

}

fun println(type: Msg, args: List<String> = listOf()) {
    println(
        when (type) {
            Msg.UNUSED_KEYS -> "Warning! Skipped next keys ${args}}"
            Msg.MISSED_ARGUMENTS -> "You should enter at least ${args.joinToString(", ") { "'$it'" }}. Use --help option to learn more."
            else -> throw IllegalArgumentException("Second argument should be a String")
        }
    )
}

fun println(type: Msg, fileName: String) {
    println(
        when (type) {
            Msg.FILE_NOT_AVAILABLE -> "Program does not have permission to access to $fileName. Aborting"
            Msg.FILE_NOT_EXIST -> "File $fileName does not exist. Aborting"
            Msg.FILE_ALREADY_EXISTS -> "Warning! File $fileName already exist. Replace it? [Y/n]"
            else -> throw IllegalArgumentException("Second argument should be a List<String>")
        }
    )
}