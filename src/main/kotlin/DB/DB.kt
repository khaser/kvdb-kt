package DB

import output.Msg
import output.println
import java.io.EOFException
import java.io.IOException
import java.io.RandomAccessFile


class DB(val fileName: String, newConfig: Config? = null) : RandomAccessFile(fileName, "rw") {

    val config = newConfig ?: readConfig() ?: throw Exception("DamagedFile")

    /** Implementation of remove option. Find node by key and clear this node. */
    fun removeKey(key: String) {
        if (key.length > config.keySize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "key"); return
        }
        val beginOfChain = getPartition(key)
        val node = try {
            findNodeInPartition(beginOfChain, key) ?: return
        } catch (error: Exception) {
            return
        }
        writeNode(node.copy(value = config.defaultValue))
    }

    /** Implementation of get option. Find partition and return node value by key. */
    fun getKeyValue(key: String): String? {
        if (key.length > config.keySize) {
            println(Msg.ILLEGAL_FIELD_SIZE, "key")
            return null
        }
        val beginOfChain = getPartition(key)
        val node = try {
            findNodeInPartition(beginOfChain, key)
        } catch (error: Exception) {
            return null
        }
        return node?.value ?: config.defaultValue
    }

    /** Implementation of set option. Find partition and change node by key. */
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
        val node = try {
            findNodeInPartition(partition, key)
        } catch (error: Exception) {
            return
        }
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

    /** Read data base config from first bytes of DB file named header. */
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

    /** Implementation of dump option. Reads all partitions node by node in memory order. */
    fun dumpAllDataBase(): Dump? {
        val partitions = try {
            List(config.partitions) {
                getNodesInPartition(getPartition(it)).dropLast(1)
            }
        } catch (error: Exception) {
            return null
        }
        return partitions.flatten().filter { it.value != config.defaultValue }.map { Pair(it.key, it.value) }
    }

    /** Find node with some key by partition begin index and key in DB. */
    private fun findNodeInPartition(index: Long, key: String): Node? {
        if (index == -1L) return null
        val curNode = readNode(index)
        return if (curNode.key == key) curNode else findNodeInPartition(curNode.nextIndex, key)
    }

    /** Find last node in partition by partition begin index. Can be null if file damaged.*/
    private fun getLastNodeInPartition(index: Long): Node {
        val curNode = readNode(index)
        return if (curNode.nextIndex == -1L) curNode else getLastNodeInPartition(curNode.nextIndex)
    }

    /** Return all nodes in partition by partition begin index. Can be null if file damaged. First node is imaginary. */
    private fun getNodesInPartition(index: Long): MutableList<Node> {
        val curNode = readNode(index)
        return if (curNode.nextIndex == -1L) mutableListOf(curNode)
        else getNodesInPartition(curNode.nextIndex).also { it.add(curNode) }
    }

    /** Return partition begin index by key. */
    private fun getPartition(key: String) = getHash(key, config) * config.nodeSize + config.configSize

    /** Return partition begin index by number of partition. */
    private fun getPartition(number: Int) = number * config.nodeSize + config.configSize.toLong()

    /** Calculate partition number for string. */
    private fun getHash(str: String, config: Config) = (kotlin.math.abs(str.hashCode()) % config.partitions).toLong()

    /** Read all node from DB under pointer. Print message and return null if failed. */
    private fun readNode(pointer: Long): Node {
        return try {
            seek(pointer)
            val key = readString()
            seek(pointer + config.keySize + Int.SIZE_BYTES)
            val value = readString()
            seek(pointer + config.keySize + config.valueSize + 2 * Int.SIZE_BYTES)
            val itIndex = readLong()
            val nextIndex = readLong()
            Node(config, key, value, itIndex, nextIndex)
        } catch (error: Exception) {
            println(Msg.FILE_DAMAGED, fileName)
            throw Exception("Damaged database")
        }
    }

    /** Write node to DB. Be careful! This function write node from node itIndex field. So you can loose data if
     * you write node with not valid itIndex. */
    fun writeNode(node: Node) {
        seek(node.itIndex)
        writeString(node.key)
        seek(node.itIndex + config.keySize + Int.SIZE_BYTES)
        writeString(node.value)
        seek(node.itIndex + config.keySize + config.valueSize + 2 * Int.SIZE_BYTES)
        writeLong(node.itIndex)
        writeLong(node.nextIndex)
    }

    /** Write string to DB under cursor. */
    fun writeString(str: String) {
        writeInt(str.length)
        writeBytes(str)
    }

    /** Read string from DB under cursor. */
    private fun readString(): String {
        val size = readInt()
        return String(ByteArray(size) { readByte() })
    }
}