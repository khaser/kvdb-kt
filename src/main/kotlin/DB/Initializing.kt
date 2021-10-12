package DB

import input.*
import output.*
import java.io.File

/** Try to open database by name and return it if success. Print fail reason on the other hand. */
fun safeOpenDB(fileName: String): DB? {
    val file = File(fileName)
    return if (file.exists()) {
        if (file.canWrite() && file.canRead()) {
            try { DB(fileName) } catch (error: Exception) { return null }
        } else {
            println(Msg.FILE_NOT_AVAILABLE, fileName); null
        }
    } else {
        println(Msg.FILE_NOT_EXIST, fileName); null
    }
}

/** Try to initialize new database and return it if success. Print fail reason on the other hand. */
fun initDataBase(fileName: String, config: Config): DB? {
    val blankDB = File(fileName)
    if (blankDB.exists()) {
        if (blankDB.canRead() && blankDB.canWrite()) {
            println(Msg.FILE_ALREADY_EXISTS, fileName)
            if (getUserAnswer(Question.OVERWRITE_FILE)) blankDB.delete() else return null
        } else {
            println(Msg.FILE_NOT_AVAILABLE, fileName); return null
        }
    }
    if (config.defaultValue.length > config.valueSize) {
        println(Msg.TOO_LARGE_DEFAULT); return null
    }
    return DB(fileName, config).also { writeInitData(it, config) }
}
fun initDataBase(fileName: String, options: Options): DB? = initDataBase(fileName, options.toDBConfig(Config()))

/** Write config to database and init every partition by void node*/
fun writeInitData(db: DB, config: Config) {
    db.setLength(0)
    db.writeInt(config.partitions)
    db.writeInt(config.keySize)
    db.writeInt(config.valueSize)
    db.writeString(config.defaultValue)
    repeat(config.partitions) {
        db.writeNode(Node(config, "", "", db.length(), -1))
    }
}