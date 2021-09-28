import java.io.File
import kotlin.math.abs

fun DB.readConfig(): DBConfig {
    val partitions = this.readInt()
    val keySize = this.readInt()
    val valueSize = this.readInt()
    val defaultValue = this.readString(valueSize)
    return DBConfig(partitions, keySize, valueSize, defaultValue)
}

fun checkIsAvailable(fileName: String): Boolean = File(fileName).let { it.canWrite() && it.canRead() }

fun DB.readString(size: Int): String = String(ByteArray(size) { this.readByte() })

fun getHash(str: String, config: DBConfig): Int = abs(str.hashCode()) % config.partitions

fun DB.readNode(config: DBConfig): Node {
    val key = this.readString(config.keySize)
    val value = this.readString(config.valueSize)
    val skipToNextNode = this.readInt()
    return Node(key, value, skipToNextNode)
}

fun DB.findNodeInChain(config: DBConfig, index: Int, key: String): Node {
    this.seek(index.toLong())
    val curNode = this.readNode(config)
    if (curNode.nextIndex == -1) return curNode
    return if (curNode.key == key) curNode else this.findNodeInChain(config, curNode.nextIndex, key)
}

fun DB.writeNode(config: DBConfig, node: Node) {
    this.writeBytes(node.key)
    this.writeBytes(node.value.padEnd(config.valueSize, ' '))
    this.writeInt(node.nextIndex)
    this.seek(this.filePointer - config.nodeSize)
}
