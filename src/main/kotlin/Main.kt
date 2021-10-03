//Loop count challenge; while - 1; for - 0
import input.parseUserInput
import java.io.File

val userManual = """
   NAME 
       kvdb-kt - tool for creating and access lightweight data base stored in single file.
   SYNOPSIS
       kvdb-kt OPERATION [OPTIONS]... DB-FILE
   OPERATIONS
       create [OPTIONS] - make new database in DB-FILE
       CREATE OPTIONS LIST
           --partitions or -p NUM (Default=5000) - set number of partitions in database. As the number of partitions increases,
                                    the memory occupied increases, but the performance improves.
           --keysize or -k NUM (Default=255) - set maximum size of KEY field for database
           --valuesize or -v NUM (Default=255) - set maximum size of VALUE field for database
           --defautvalue or -d STR (Default='')- set default value for VALUE field
       get KEY - get value by KEY in DB-FILE
       set KEY VALUE - set VALUE by KEY in DB-FILE
       dump - print all database in format KEY=VALUE
       shrink [OPTIONS] - change your database configuration with options matching 'create'
       config - print database configuration
""".trimIndent()

/** Check permissions for write and read from DB FILE */
fun checkIsAvailable(fileName: String) = File(fileName).let { it.canWrite() && it.canRead() }

//fun main(args: Array<String>) {
fun main() {
    val args = readLine()!!.split(' ')
    //Is help call check
    if (args.contains("-h") || args.contains("--help")) {
        println(userManual); return
    }
    //Primary check
    if (args.size < 2) {
        println(Msg.MISSED_ARGUMENTS, listOf("mode", "data base file")); return
    }
    val mode = args[0]
    val fileName = args.last()

    //Check count of arguments for every mode
    when (mode) {
        "get" -> {
            if (args.size < 3) {
                println(Msg.MISSED_ARGUMENTS, listOf("get", "key", "data base file"))
                return
            }
            if (args.size != 3) println(Msg.MISSED_ARGUMENTS, args.slice(2 until args.size - 1))
        }
        "set" -> {
            if (args.size < 4) {
                println(Msg.MISSED_ARGUMENTS, listOf("set", "key", "value", "data base file"))
                return
            }
            if (args.size != 4) println(Msg.MISSED_ARGUMENTS, args.slice(3 until args.size - 1))
        }
        "config", "dump" -> {
            if (args.size != 2) println(Msg.MISSED_ARGUMENTS, args.slice(1 until args.size - 1))
        }
    }

    //Check is file available for different actions or creating DB
    when (mode) {
        "create" -> {
            val options = parseUserInput(args.slice(1 until args.size - 1))
            initDataBase(fileName, options)
            return
        }
        else -> {
            if (!File(fileName).exists()) {
                println()
                println(Msg.FILE_NOT_EXIST, fileName); return
            }
            if (!checkIsAvailable(fileName)) {
                println(Msg.FILE_NOT_AVAILABLE, fileName); return
            }
        }
    }
    val db = DB(fileName)

    //Different actions for each mode
    when (mode) {
        "set" -> {
            val key = args[1]
            val value = args[2]
            db.setKeyValue(key, value)
        }
        "get" -> {
            val key = args[1]
            println(db.getKeyValue(key))
        }
        "dump" -> {
            db.dumpAllDataBase().forEach { println(it) }
        }
        "shrink" -> {
            val newConfig = parseUserInput(args.slice(1 until args.size - 1))
            db.shrink(newConfig)
        }
        "config" -> {
            db.readConfig().also {
                if (it != null) println(it.copy(defaultValue = "'" + it.defaultValue.trim() + "'"))
                else println("Config is undefined")
            }
        }
        else -> {
            println(userManual)
        }
    }
    db.close()
}