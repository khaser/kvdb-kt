import java.io.File
import kotlin.math.abs

/** Read DBConfig from first N bytes of DB file */
fun DB.readConfig(): DBConfig {
    val partitions = this.readInt()
    val keySize = this.readInt()
    val valueSize = this.readInt()
    val defaultValue = this.readString(valueSize)
    return DBConfig(partitions, keySize, valueSize, defaultValue)
}

/** Check permissions for write and read from DB FILE */
fun checkIsAvailable(fileName: String) = File(fileName).let { it.canWrite() && it.canRead() }

/** Read N bytes from DB under cursor */
fun DB.readString(size: Int) = String(ByteArray(size) { this.readByte() })

/** Calculate partition number for string */
fun getHash(str: String, config: DBConfig) = abs(str.hashCode()) % config.partitions

/** Read all node from DB under cursor*/
fun DB.readNode(config: DBConfig): Node {
    val key = this.readString(config.keySize)
    val value = this.readString(config.valueSize)
    val skipToNextNode = this.readInt()
    return Node(key, value, skipToNextNode)
}

/** Find node by key and partition index in DB */
fun DB.findNodeInPartition(config: DBConfig, index: Int, key: String): Node {
    this.seek(index.toLong())
    val curNode = this.readNode(config)
    if (curNode.nextIndex == -1) return curNode
    return if (curNode.key == key) curNode else this.findNodeInPartition(config, curNode.nextIndex, key)
}

/** Overwrite node under cursor in DB */
fun DB.writeNode(config: DBConfig, node: Node) {
    this.writeBytes(node.key)
    this.writeBytes(node.value.padEnd(config.valueSize, ' '))
    this.writeInt(node.nextIndex)
    this.seek(this.filePointer - config.nodeSize)
}
