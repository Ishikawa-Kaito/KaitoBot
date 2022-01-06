package cn.zeshawn.kaitobot.util

import cn.hutool.core.io.file.FileReader
import cn.hutool.core.io.file.FileWriter
import cn.hutool.core.net.URLDecoder
import cn.zeshawn.kaitobot.KaitoMind
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Synchronized
fun File.getContext(): String {
    return FileReader.create(this, Charsets.UTF_8).readString()
}

@Synchronized
fun File.writeClassToJson(context: Any, mapper: ObjectMapper = KaitoMind.mapper) {
    FileWriter.create(this).write(mapper.writeValueAsString(context), false)
}

inline fun <reified T> File.loadClassFromJson(mapper: ObjectMapper = KaitoMind.mapper): T {
    require(exists())
    return mapper.readValue(getContext())
}

fun File.createBackupFile() {
    require(exists()) { "$name 不存在" }

    if (isDirectory) return

    copyAndRename("$name.backup")
}

fun File.copyAndRename(name: String) {
    require(exists()) { "$name 不存在" }

    if (isDirectory) return

    val backup = File(parent, name)
    backup.createNewFile()
    Files.copy(toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING)
}

object FileUtil {
    fun getJarLocation(): File {
        var path: String = cn.zeshawn.kaitobot.KaitoApp::class.java.protectionDomain.codeSource.location.path
        if (System.getProperty("os.name").lowercase().contains("dows")) {
            path = path.substring(1)
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("/"))
            return File(URLDecoder.decode(path, Charsets.UTF_8))
        }
        return File(URLDecoder.decode(path.replace("target/classes/", ""), Charsets.UTF_8))
    }

    fun newFile(path: String): File {
        val file = File(path)
        if (!file.exists()) {
            if (file.parentFile != null) {
                file.parentFile.mkdirs()
            }
            file.createNewFile()
        }
        return file
    }
}


