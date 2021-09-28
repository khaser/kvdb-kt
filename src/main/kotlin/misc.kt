import kotlin.math.abs

fun DB.readString(size: Int): String {
    return String(ByteArray(size) {this.readByte()})
}

fun readNode(db: DB, config: DBConfig): Node {
    val key = db.readString(config.keySize)
    val value = db.readString(config.valueSize)
    val skipToNextNode = db.readInt()
    return Node(key, value, skipToNextNode)
}

fun getHash(str: String, config: DBConfig): Int {
    return abs(str.hashCode()) % config.partitions
}

fun DB.findNodeInChain(config: DBConfig, index: Int, key: String): Node {
    this.seek(index.toLong())
    val curNode = readNode(this, config)
    if (curNode.nextIndex == -1) return curNode
    return if (curNode.key == key) curNode else this.findNodeInChain(config, curNode.nextIndex, key)
}

fun DB.writeNode(config: DBConfig, node: Node) {
    this.writeBytes(node.key)
    this.writeBytes(node.value.padEnd(config.valueSize, ' '))
    this.writeInt(node.nextIndex)
    this.seek(this.filePointer - config.nodeSize)
}
