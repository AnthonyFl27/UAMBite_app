package com.uambite.app.data.realtime

data class StompFrame(
    val command: String,
    val headers: Map<String, String>,
    val body: String?
) {
    fun header(name: String): String? = headers[name]
        ?: headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
}

object StompFrames {
    private const val NULL = '\u0000'

    fun build(command: String, headers: Map<String, String>, body: String?): String {
        val sb = StringBuilder()
        sb.append(command).append('\n')
        headers.forEach { (k, v) -> sb.append(k).append(':').append(v).append('\n') }
        sb.append('\n')
        if (body != null) sb.append(body)
        sb.append(NULL)
        return sb.toString()
    }

    fun parse(raw: String): StompFrame? {
        if (raw.isEmpty()) return null
        val nl = raw.indexOf('\n')
        if (nl < 0) return null
        val command = raw.substring(0, nl)
        val rest = raw.substring(nl + 1)

        val blankIdx = rest.indexOf("\n\n")
        if (blankIdx < 0) return null
        val headerBlock = rest.substring(0, blankIdx)
        var body = rest.substring(blankIdx + 2)
        if (body.endsWith(NULL)) body = body.dropLast(1)

        val headers = if (headerBlock.isEmpty()) emptyMap()
        else headerBlock.split('\n').mapNotNull { line ->
            val colon = line.indexOf(':')
            if (colon < 0) null else line.substring(0, colon) to line.substring(colon + 1)
        }.toMap()
        return StompFrame(command, headers, body.ifEmpty { null })
    }
}
