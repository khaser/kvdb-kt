package DB

import input.Option
import input.Options
import output.Msg
import output.Question
import output.getUserAnswer
import output.println
import java.io.File

typealias Dump = List<Pair<String, String>>

/** Dump all DB and return new DB by adding all entries with new config
 *  Warning! Old DB is becoming outdated*/
fun DB.shrink(options: Options): DB? {
    var dump = dumpAllDataBase() ?: return null
    val config = this.config
    var newConfig = options.toDBConfig(config)
    //Check if newConfig applicable to dump
    if (newConfig.defaultValue.length > newConfig.valueSize) {
        println(Msg.TOO_LARGE_DEFAULT)
        if (config.defaultValue.length < newConfig.valueSize &&
            newConfig.defaultValue != config.defaultValue &&
            getUserAnswer(Question.USE_OLD_DEFAULT)
        ) {
            newConfig = options.toMutableMap().also { it.remove(Option.DEFAULT_VALUE) }.toMap().toDBConfig(config)
        } else return null
    }
    if (dump.any { it.first.length > newConfig.keySize || it.second.length > newConfig.valueSize }) {
        println(Msg.TOO_SMALL_FIELDS)
        if (!getUserAnswer(Question.DROP_SOME_ENTRIES)) return null
        dump = dump.filter { it.first.length < newConfig.keySize && it.second.length < newConfig.valueSize }
    }

    //Deleting old database. All data stored in dump(RAM) now.
    this.close()
    File(fileName).delete()

    //Creating new database and initialization with dump
    val newDB = initDataBase(fileName, newConfig)
    requireNotNull(newDB)
    dump.forEach { newDB.setKeyValue(it.first, it.second) }
    return newDB
}
