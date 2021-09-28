
fun dumpAllDataBase(db: DB, config: DBConfig): Map<String, String> {
    db.seek(config.configSize.toLong())
    val result: MutableMap<String, String> = mutableMapOf()
    val cntNodes = (db.length().toInt() - config.configSize) / config.nodeSize
    repeat(cntNodes) {
        val node = db.readNode(config)
    }
    return result
}
