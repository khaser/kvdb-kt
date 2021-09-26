import java.io.RandomAccessFile

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

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("You must input at least mode and data base file")
        return
    }
    val mode = args[0]
    val file = args.last()
//    val modificators = args.slice(1 until args.size)

    val db = DB(file, "rws")
//    val config = when (mode) {
//        "run" -> readConfig(db)
//        else -> (DBConfig(1, 15, 15, "DEFAULTVALUEXXX"))
//    }

    val config = DBConfig(1, 15, 15, "DEFAULTVALUEXXX")
    db.setLength(0)
    db.initDataBase(config)

    when (mode) {
        "create" -> {db.setLength(0); db.initDataBase(config)}
//        "set" -> setKeyValue(db, config)
//        "get" -> getKeyValue(db, config)
        "run" -> {
            var action = ""
            while (action != "end") {
                action = readLine()!!
                when (action){
                    "get" -> {println("THIS IS GET"); println(db.getKeyValue(config, readLine()!!))}
                    "set" -> {println("THIS IS SET"); db.setKeyValue(config, readLine()!!, readLine()!!)}
                }
            }
        }
    }
    db.close()
}
