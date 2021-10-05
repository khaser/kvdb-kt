package DB

import java.io.File
import input.*
import output.*

/** Dump all DB and return new DB by adding all entries with new config
 *  Warning! Old DB is becoming outdated*/
fun DB.shrink(options: Options): DB? {
    var dump = dumpAllDataBase()

    val config = this.config
    val newConfig = Config(
        options[Option.PARTITIONS]?.toInt() ?: config.partitions,
        options[Option.KEYS_SIZE]?.toInt() ?: config.keySize,
        options[Option.VALUES_SIZE]?.toInt() ?: config.valueSize,
        options[Option.DEFAULT_VALUE] ?: config.defaultValue
    )
    if (newConfig.defaultValue.length > newConfig.valueSize) {
        println(Msg.TOO_LARGE_DEFAULT); return null
    }
    if (dump.any{it.first.length > newConfig.keySize || it.second.length > newConfig.valueSize}) {
        println(Msg.TOO_SMALL_FIELDS)
        if ((kotlin.io.readLine() ?: "n").uppercase() != "Y") return null
        dump = dump.filter {it.first.length < newConfig.keySize && it.second.length < newConfig.valueSize}
    }
    this.close()
    File(fileName).delete()

    val newDB = initDataBase(fileName, newConfig)
    requireNotNull(newDB)
    dump.forEach { newDB.setKeyValue(it.first, it.second) }
    return newDB
}