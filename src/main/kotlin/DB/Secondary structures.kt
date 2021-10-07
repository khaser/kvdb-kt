package DB

import input.Options

/** Convert Options to DB config, using default values if they are not declared in options*/
fun Options.toDBConfig(defaultConfig: Config) = Config(
    this[input.Option.PARTITIONS]?.toInt() ?: defaultConfig.partitions,
    this[input.Option.KEYS_SIZE]?.toInt() ?: defaultConfig.keySize,
    this[input.Option.VALUES_SIZE]?.toInt() ?: defaultConfig.valueSize,
    this[input.Option.DEFAULT_VALUE] ?: defaultConfig.defaultValue
)

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
