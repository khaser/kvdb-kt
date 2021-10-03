package DB

import input.Options
import output.Msg
import output.println
import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile


/** Dump all DB and return new DB by adding all entries with new config
 *  Warning! Old DB is becoming outdated*/
fun DB.shrink(options: Options): DB {
    val dump = dumpAllDataBase()
    this.close()
    File(fileName).delete()
    val newDB = initDataBase(fileName, options)
    requireNotNull(newDB)
    dump.forEach { newDB.setKeyValue(it.key, it.value) }
    return newDB
}

data class Config(
    val partitions: Int = 100,
    val keySize: Int = 255,
    val valueSize: Int = 255,
    val defaultValue: String = ""
) {
    private val cntIntInHeader = 3
    val nodeSize: Int = keySize + valueSize + Int.SIZE_BYTES
    val configSize: Int = cntIntInHeader * Int.SIZE_BYTES + defaultValue.length
}

class DB(val fileName: String, newConfig: Config? = null) : RandomAccessFile(fileName, "rw") {

    /** Class for storing entry in DB */
    data class Node(
        private val config: Config,
        private val preKey: String,
        private val preValue: String,
        val nextIndex: Int
    ) {
        val key = preKey.padEnd(config.keySize)
        val value = preValue.padEnd(config.valueSize)
    }

    /** Class for first N DB bytes for configuring DB structure*/
    val config = newConfig ?: readConfig() ?: throw Exception("DamagedFile")

    /** Find node by key and clear this node */
    fun removeKey(key: String) {
        if (key.length > config.keySize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "key"); return
        }
        val beginOfChain = getBeginOfChain(key)
        val node = findNodeInPartition(beginOfChain, key) ?: return
        seek(filePointer - config.nodeSize)
        writeNode(Node(config, "", config.defaultValue, node.nextIndex))
    }

    /** Find partition and return node value by key*/
    fun getKeyValue(key: String): String? {
        if (key.length > config.keySize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "key")
            return null
        }
        val beginOfChain = getBeginOfChain(key)
        val node = findNodeInPartition(beginOfChain, key)
        return (node?.value ?: config.defaultValue).trim()
    }

    /** Find partition and change node by key*/
    fun setKeyValue(key: String, value: String) {
        if (key.length > config.keySize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "key"); return
        }
        if (value.length > config.valueSize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "value"); return
        }
        val beginOfChain = getBeginOfChain(key)
        val node = findNodeInPartition(beginOfChain, key)
        if (node != null) {
            //Change existing key
            seek(filePointer - config.nodeSize)
            writeNode(Node(config, key, value, node.nextIndex))
        } else {
            //Create new key
            seek(filePointer - config.nodeSize)
            writeNode(Node(config, key, value, length().toInt()))
            seek(length())
            writeNode(Node(config, "", config.defaultValue, -1))
        }
    }

    /** Read DBConfig from first N bytes of DB file */
    fun readConfig(): Config? {
        seek(0)
        val newConfig: Config?
        try {
            val partitions = readInt()
            val keySize = readInt()
            val valueSize = readInt()
            val defaultValue = readString(valueSize)
            newConfig = Config(partitions, keySize, valueSize, defaultValue)
        } catch (error: Exception) {
            when (error) {
                is EOFException -> println(Msg.FILE_DAMAGED, fileName)
                is IOException -> println(Msg.FILE_NOT_AVAILABLE, fileName)
            }
            return null
        }
        return newConfig
    }

    /** Implementation for dump option. Reads all partitions node by node in memory order*/
    fun dumpAllDataBase(): Map<String, String> {
        seek(config.configSize.toLong())
        val cntNodes = (length().toInt() - config.configSize) / config.nodeSize
        return List(cntNodes) {
            val node = readNode(); Pair(
            node.key.trim(),
            node.value.trim()
        )
        }.filter { it.first != config.defaultValue.trim() }.toMap()
    }

    /** Find node by key and partition index in DB */
    private fun findNodeInPartition(index: Int, key: String): Node? {
        seek(index.toLong())
        val curNode = readNode()
        if (curNode.nextIndex == -1) return null
        return if (curNode.key.trim() == key) curNode else findNodeInPartition(curNode.nextIndex, key)
    }

    /** Read N bytes from DB under cursor */
    private fun readString(size: Int) = String(ByteArray(size) { readByte() })

    /** Calculate index of begin of chain, which content key */
    private fun getBeginOfChain(key: String) = getHash(key, config) * config.nodeSize + config.configSize

    /** Calculate partition number for string */
    private fun getHash(str: String, config: Config) = kotlin.math.abs(str.hashCode()) % config.partitions

    /** Read all node from DB under cursor*/
    private fun readNode(): Node {
        val key = readString(config.keySize)
        val value = readString(config.valueSize)
        val skipToNextNode = readInt()
        return Node(config, key, value, skipToNextNode)
    }

    /** Overwrite node under cursor in DB */
    private fun writeNode(node: Node) {
        writeBytes(node.key)
        writeBytes(node.value)
        writeInt(node.nextIndex)
        seek(filePointer - config.nodeSize)
    }
}