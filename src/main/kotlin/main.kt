import input.Option
import java.io.RandomAccessFile
import java.nio.file.Files
import input.parseAllKeys

val userManual = """
   TODO MANUAL 
""".trimIndent()

data class Node(val key: String, val value: String, val nextIndex: Int)
data class DBConfig(val partitions: Int, val keySize: Int, val valueSize: Int, val defaultValue: String,
                    val nodeSize: Int = keySize + valueSize + Int.SIZE_BYTES,
                    val configSize: Int = 3 * Int.SIZE_BYTES + defaultValue.length)

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

fun readConfig(db: DB): DBConfig {
    val partitions = db.readInt()
    val keySize = db.readInt()
    val valueSize = db.readInt()
    val defaultValue = db.readString(valueSize)
    return DBConfig(partitions, keySize, valueSize, defaultValue)
}

fun DB.getKeyValue(config: DBConfig, key: String): String {
    key.padEnd(config.keySize, ' ').also{
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
//    if (mode != "create" && Files.isReadable(pfile)) {
//    }
    val db = DB(file, "rw")

    when (mode) {
        "create" -> {
            val modificators = parseAllKeys(args.slice(1 until args.size - 1))
            val partitions = modificators[Option.PARTITIONS]?.toInt() ?: 5000
            val keySize = modificators[Option.KEYS_SIZE]?.toInt() ?: 255
            val valueSize = modificators[Option.VALUE_SIZE]?.toInt() ?: 255
            val defaultValue = (modificators[Option.DEFAULT_VALUE] ?: "").padEnd(valueSize)
            val config = DBConfig(partitions, keySize, valueSize, defaultValue)
            db.setLength(0);
            db.initDataBase(config)
        }
        "set" -> {
            val config = readConfig(db)
            if (args.size < 4) {
                println(userManual)
                return
            }
            val key = args[1]
            val value = args[2]
            db.setKeyValue(config, key, value)
        }
        "get" -> {
            val config = readConfig(db)
            if (args.size < 3) {
                println(userManual)
                return
            }
            val key = args[1]
            println(db.getKeyValue(config, key))
        }
        else -> {println(userManual)}
    }
    db.close()
}
