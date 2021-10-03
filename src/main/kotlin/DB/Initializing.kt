package DB

import input.*
import output.Msg
import java.io.File

fun saveOpenDB(fileName: String): DB? {
    val file = File(fileName)
    return if (file.exists()) {
        if (file.canWrite() && file.canRead()) {
            try { DB(fileName) } catch (error: Exception) { return null }
        } else {
            output.println(Msg.FILE_NOT_AVAILABLE, fileName); null
        }
    } else {
        output.println(Msg.FILE_NOT_EXIST, fileName); null
    }
}

/** Write config to database and init every partition by void node*/
fun initDataBase(fileName: String, config: Config): DB? {
    val blankDB = File(fileName)
    if (blankDB.exists()) {
        if (blankDB.canRead() && blankDB.canWrite()) {
            output.println(Msg.FILE_ALREADY_EXISTS, fileName)
            if ((readLine() ?: "n").uppercase() != "Y") return null else blankDB.delete()
        } else {
            output.println(Msg.FILE_NOT_AVAILABLE, fileName); return null
        }
    }
    val db = DB(fileName, config)
    db.setLength(0)
    db.writeInt(config.partitions)
    db.writeInt(config.keySize)
    db.writeInt(config.valueSize)
    db.writeBytes(config.defaultValue)
    repeat(config.partitions) {
        db.writeBytes("".padEnd(config.keySize))
        db.writeBytes(config.defaultValue)
        db.writeInt(-1)
    }
    return db
}
fun initDataBase(fileName: String, options: Options): DB? = initDataBase(fileName, options.toDBConfig())