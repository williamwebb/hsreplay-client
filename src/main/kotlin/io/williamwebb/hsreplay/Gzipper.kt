package io.williamwebb.hsreplay

import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


/**
 * Created by williamwebb on 6/6/17.
 */
object Gzipper {

    @Throws(IOException::class)
    fun compress(str: String): ByteArray? {
        if (str.isEmpty()) {
            return null
        }
        val obj = ByteArrayOutputStream()
        val gzip = GZIPOutputStream(obj)
        gzip.write(str.toByteArray(charset("UTF-8")))
        gzip.close()
        return obj.toByteArray()
    }

    @Throws(IOException::class)
    fun decompress(compressed: ByteArray): String {
        if (compressed.isEmpty()) {
            return ""
        }
        var outStr = ""
        if (isCompressed(compressed)) {
            val gis = GZIPInputStream(ByteArrayInputStream(compressed))
            val bufferedReader = BufferedReader(InputStreamReader(gis, "UTF-8"))

            var line: String = bufferedReader.readLine()
            do {
                outStr += line
                line = bufferedReader.readLine()
            } while(!line.isNullOrBlank())
        } else {
            outStr = String(compressed)
        }
        return outStr
    }

    fun isCompressed(compressed: ByteArray): Boolean {
        return compressed[0] == GZIPInputStream.GZIP_MAGIC.toByte() && compressed[1] == (GZIPInputStream.GZIP_MAGIC shr 8).toByte()
    }
}