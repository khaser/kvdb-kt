package DB

import output.Msg
import output.println
import java.io.EOFException
import java.io.IOException
import java.io.RandomAccessFile



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

/** Class for storing entry in DB */
data class Node(private val config: Config, val key: String, val value: String, val itIndex: Long, val nextIndex: Long)

class DB(val fileName: String, newConfig: Config? = null) : RandomAccessFile(fileName, "rw") {

    /** Class for first N DB bytes for configuring DB structure*/
    val config = newConfig ?: readConfig() ?: throw Exception("DamagedFile")

    /** Find node by key and clear this node */
    fun removeKey(key: String) {
        if (key.length > config.keySize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "key"); return
        }
        val beginOfChain = getPartition(key)
        val node = findNodeInPartition(beginOfChain, key) ?: return
        writeNode(node.copy(value = config.defaultValue))
    }

    /** Find partition and return node value by key*/
    fun getKeyValue(key: String): String? {
        if (key.length > config.keySize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "key")
            return null
        }
        val beginOfChain = getPartition(key)
        val node = findNodeInPartition(beginOfChain, key)
        return node?.value ?: config.defaultValue
    }

    /** Find partition and change node by key*/
    fun setKeyValue(key: String, value: String) {
        var ok = true
        if (key.length > config.keySize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "key"); ok = false
        }
        if (value.length > config.valueSize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "value"); ok = false
        }
        if (!ok) return
        val partition = getPartition(key)
        val node = findNodeInPartition(partition, key)
        if (node != null) {
            //Change existing key
            writeNode(node.copy(key = key, value = value))
        } else {
            //Create new key
            val lastNode = getLastNodeInPartition(partition)
            writeNode(lastNode.copy(nextIndex = length()))
            writeNode(Node(config, key, value, length(), -1))
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
            val defaultValue = readString()
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
    fun dumpAllDataBase(): List<Pair<String, String>> {
        return List(config.partitions) {
            getNodesInPartition(getPartition(it)).dropLast(1)
        }.flatten().filter { it.value != config.defaultValue }.map { Pair(it.key, it.value) }
    }

    /** Find node by partition and key in DB */
    private fun findNodeInPartition(index: Long, key: String): Node? {
        if (index == -1L) return null
        val curNode = readNode(index)
        return if (curNode.key == key) curNode else findNodeInPartition(curNode.nextIndex, key)
    }

    /** Find last node in partition */
    private fun getLastNodeInPartition(index: Long): Node {
        val curNode = readNode(index)
        return if (curNode.nextIndex == -1L) curNode else getLastNodeInPartition(curNode.nextIndex)
    }

    /** Return all nodes in partition */
    private fun getNodesInPartition(index: Long): MutableList<Node> {
        val curNode = readNode(index)
        return if (curNode.nextIndex == -1L) mutableListOf(curNode)
        else getNodesInPartition(curNode.nextIndex).also { it.add(curNode) }
    }

    /** Calculate index of begin of chain, which content key */
    private fun getPartition(key: String) = getHash(key, config) * config.nodeSize + config.configSize
    private fun getPartition(number: Int) = number * config.nodeSize + config.configSize.toLong()

    /** Calculate partition number for string */
    private fun getHash(str: String, config: Config) = (kotlin.math.abs(str.hashCode()) % config.partitions).toLong()

    /** Read all node from DB under cursor*/
    private fun readNode(pointer: Long): Node {
        seek(pointer)
        val key = readString()
        seek(pointer + config.keySize + Int.SIZE_BYTES)
        val value = readString()
        seek(pointer + config.keySize + config.valueSize + 2 * Int.SIZE_BYTES)
        val itIndex = readLong()
        val nextIndex = readLong()
        return Node(config, key, value, itIndex, nextIndex)
    }

    /** Write node to DB*/
    fun writeNode(node: Node) {
        seek(node.itIndex)
        writeString(node.key)
        seek(node.itIndex + config.keySize + Int.SIZE_BYTES)
        writeString(node.value)
        seek(node.itIndex + config.keySize + config.valueSize + 2 * Int.SIZE_BYTES)
        writeLong(node.itIndex)
        writeLong(node.nextIndex)
    }

    fun writeString(str: String) {
        writeInt(str.length)
        writeBytes(str)
    }

    /** Read N bytes from DB under cursor */
    private fun readString(): String {
        val size = readInt()
        return String(ByteArray(size) { readByte() })
    }
}