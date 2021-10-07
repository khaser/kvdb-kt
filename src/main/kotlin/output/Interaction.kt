package output

enum class Question {
    USE_OLD_DEFAULT { override val str = "Use old default instead of new?" },
    DROP_SOME_ENTRIES { override val str = "Force shrink and drop some entries?" },
    OVERWRITE_FILE { override val str = "Overwrite this file?" };
    abstract val str: String
}

/** Ask the question to user and return True if positive, False on the other hand*/
fun getUserAnswer(question: Question): Boolean {
    println("${question.str} [y/N]")
    return ((readLine() ?: "n").trim().uppercase() == "Y")
}

/** FOR TESTING ONLY!. Only ask the question to user*/
fun println(question: Question) = println("${question.str} [y/N]")
