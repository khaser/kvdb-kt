fun DB.dumpAllDataBase(config: DBConfig): Map<String, String> {
    this.seek(config.configSize.toLong())
    val cntNodes = (this.length().toInt() - config.configSize) / config.nodeSize
    return List(cntNodes) {
        val node = this.readNode(config); Pair(
        node.key.trim(),
        node.value.trim()
    )
    }.filter { it.first != config.defaultValue.trim() }.toMap()
}

fun DB.shrink(config: DBConfig, newConfig: DBConfig) {
    val dump = this.dumpAllDataBase(config)
    this.initDataBase(newConfig)
    dump.forEach { this.setKeyValue(newConfig, it.key, it.value) }
}