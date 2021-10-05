package DB

/** Class contained header for configuring DB structure. */
data class Config(
    val partitions: Int = 100,
    val keySize: Int = 255,
    val valueSize: Int = 255,
    val defaultValue: String = ""
) {
    private val cntSystemBytesInHeader = 4 * Int.SIZE_BYTES
    private val cntSystemBytesInNode = 2 * Int.SIZE_BYTES + 2 * Long.SIZE_BYTES
    val nodeSize: Int = keySize + valueSize + cntSystemBytesInNode
    val configSize: Int = defaultValue.length + cntSystemBytesInHeader
}

/** Class for storing entry in DB. */
data class Node(private val config: Config, val key: String, val value: String, val itIndex: Long, val nextIndex: Long)
