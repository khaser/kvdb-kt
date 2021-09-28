//Loop count challenge; while - 1; for - 0
import input.Option
import input.parseAllKeys
import java.io.RandomAccessFile

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
""".trimIndent()

data class Node(val key: String, val value: String, val nextIndex: Int)
data class DBConfig(
    val partitions: Int, val keySize: Int, val valueSize: Int, val defaultValue: String,
    val nodeSize: Int = keySize + valueSize + Int.SIZE_BYTES,
    val configSize: Int = 3 * Int.SIZE_BYTES + defaultValue.length
)

typealias DB = RandomAccessFile

fun DB.initDataBase(config: DBConfig) {
    this.writeInt(config.partitions)
    this.writeInt(config.keySize)
    this.writeInt(config.valueSize)
    this.writeBytes(config.defaultValue)
    repeat(config.partitions) {
        this.writeBytes("".padEnd(config.keySize))
        this.writeBytes(config.defaultValue)
        this.writeInt(-1)
    }
}

fun DB.getKeyValue(config: DBConfig, key: String): String {
    key.padEnd(config.keySize, ' ').also {
        val beginOfChain = getHash(it, config) * config.nodeSize + config.configSize
        val node = this.findNodeInChain(config, beginOfChain, it)
        return if (it == node.key) node.value else config.defaultValue
    }
}

fun DB.setKeyValue(config: DBConfig, key: String, value: String) {
    val fullKey = key.padEnd(config.keySize)
    val beginOfChain = getHash(fullKey, config) * config.nodeSize + config.configSize
    val node = this.findNodeInChain(config, beginOfChain, fullKey)
    if (node.key == fullKey) {
        //Change existing key
        this.seek(this.filePointer - config.nodeSize)
        this.writeNode(config, Node(fullKey, value, node.nextIndex))
    } else {
        //Create new key
        this.seek(this.filePointer - config.nodeSize)
        this.writeNode(config, Node(node.key, node.value, this.length().toInt()))
        this.seek(this.length())
        this.writeNode(config, Node(fullKey, value, -1))
    }
}


fun main() {
    val args = readLine()!!.split(' ')
    //Primary check
    if (args.size < 2) {
        println("You must input at least mode and data base file")
        println(userManual)
        return
    }
    val mode = args[0]
    val file = args.last()
    //Check is file available for different actions
    when (mode) {
        "create" -> if (checkIsAvailable(file)) {
            println("Warning file $file already exist. Replace it? [Y/n]")
            if ((readLine() ?: "n").uppercase() != "Y") {
                println("Aborting"); return
            }
        }
        else -> if (!checkIsAvailable(file)) {
            println("Database $file is not available"); return
        }
    }
    val db = DB(file, "rw")

    //Different actions for each mode
    when (mode) {
        "create" -> {
            val options = parseAllKeys(args.slice(1 until args.size - 1))
            val partitions = options[Option.PARTITIONS]?.toInt() ?: 5000
            val keySize = options[Option.KEYS_SIZE]?.toInt() ?: 255
            val valueSize = options[Option.VALUES_SIZE]?.toInt() ?: 255
            val defaultValue = (options[Option.DEFAULT_VALUE] ?: "").padEnd(valueSize)
            if (defaultValue.length != valueSize) {
                println("Aborting. Default value length must not be more, than value size.")
                return
            }
            val config = DBConfig(partitions, keySize, valueSize, defaultValue)
            db.setLength(0);
            db.initDataBase(config)
        }
        "set" -> {
            val config = db.readConfig()
            if (args.size < 4) {
                println(userManual)
                return
            }
            val key = args[1]
            val value = args[2]
            if (key.length > config.keySize) {
                println("Aborting. Key length must not be more, than key size")
                return
            }
            if (value.length > config.valueSize) {
                println("Aborting. Value length must not be more, than value size")
                return
            }
            db.setKeyValue(config, key, value)
        }
        "get" -> {
            val config = db.readConfig()
            if (args.size < 3) {
                println(userManual)
                return
            }
            val key = args[1]
            if (key.length > config.keySize) {
                println("Aborting. Key length must not be more, than key size")
                return
            }
            println(db.getKeyValue(config, key))
        }
        "dump" -> {
            db.dumpAllDataBase(db.readConfig()).forEach { println(it) }
        }
        else -> {
            println(userManual)
        }
    }
    db.close()
}