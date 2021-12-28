package cn.zeshawn.kaitobot.util

object LogUtil {

    internal fun formatStacktrace(exception: Throwable, packageFilter: String? = null, simpleMode: Boolean): String {
        if (simpleMode) return exception.stackTraceToString()
        return buildString {
            val msg = exception.localizedMessage
            append("========================= 发生了错误 =========================")
            append("异常类型 ▶")
            append(exception.javaClass.name)
            append(if (msg == null || msg.isEmpty()) "没有详细信息" else msg)
            // org.serverct.parrot.plugin.Plugin
            var currentPackage = ""
            for (elem in exception.stackTrace) {
                val key = elem.className
                var pass = true
                if (packageFilter != null) {
                    pass = key.contains(packageFilter)
                }
                val nameSet = key.split("[.]").toTypedArray()
                val className = nameSet[nameSet.size - 1]
                val packageSet = arrayOfNulls<String>(nameSet.size - 2)
                System.arraycopy(nameSet, 0, packageSet, 0, nameSet.size - 2)
                val packageName = StringBuilder()
                for ((counter, nameElem) in packageSet.withIndex()) {
                    packageName.append(nameElem)
                    if (counter < packageSet.size - 1) {
                        packageName.append(".")
                    }
                }
                if (pass) {
                    if (packageName.toString() != currentPackage) {
                        currentPackage = packageName.toString()
                        append("")
                        append("包 $packageName ▶")
                    }
                    append("  ▶ 在类 ${className}, 方法 ${elem.methodName} (${elem.fileName}) 行 ${elem.lineNumber}")
                }
            }
            append("========================= 发生了错误 =========================")
        }
    }
}