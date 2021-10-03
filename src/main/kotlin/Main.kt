//Loop count challenge; while - 1; for - 0
import input.parseUserInput
import input.checkArgsCount
import java.io.File
import output.*
import java.io.IOException

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

//fun main(args: Array<String>) {
fun main() {
    val args = readLine()!!.split(' ').toTypedArray()
    //Is help call check
    if (args.contains("-h") || args.contains("--help")) {
        println(userManual); return
    }
    //Check count of arguments for every mode
    checkArgsCount(args) ?: return

    val mode = args[0]
    val fileName = args.last()

    //Check is file available for different actions or creating DB
    if (mode == "create") {
        val options = parseUserInput(args.slice(1 until args.size - 1))
        initDataBase(fileName, options)
        return
    }

    val db = saveOpenDB(fileName) ?: return

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