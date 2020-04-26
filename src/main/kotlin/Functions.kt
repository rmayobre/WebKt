import kotlin.experimental.xor

/** Convert Short to ByteArray. */
internal fun Short.toByteArray(): ByteArray = toByteArray(2)

/** Convert Int to ByteArray. */
internal fun Int.toByteArray(): ByteArray = toByteArray(4)

/** Convert Long to ByteArray. */
internal fun Long.toByteArray(): ByteArray = toByteArray(8)

/**
 * Create a ByteArray from the provided Number
 * @param length Byte length of Number.
 */
internal fun Number.toByteArray(length: Int): ByteArray {
    val data = ByteArray(length)
    for (i in 0 until length)
        data[i] = (this.toLong() shr 8 * (length - i - 1) and 0xFF).toByte()
    return data
}

/**
 * Masking the provided ByteArray with the xor algorithm declared in
 * RFC 6455.
 *
 *  j                   = i MOD 4
 *  transformed-octet-i = original-octet-i XOR masking-key-octet-j
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.3">RFC 6455, Section 5.3 (Client-to-Server Masking)</a>
 */
internal fun ByteArray.applyMask(key: Int): ByteArray {
    val maskingKey = key.toByteArray()
    for (i in indices) {
        this[i] = this[i] xor maskingKey[i % 4]
    }
    return this
}

/** Characters held within a base64 encoded String. */
private const val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

/**
 * Convert a ByteArray into a Base64 encoded String.
 * @see <a href="https://tools.ietf.org/html/rfc4648">RFC 4648</a>
 */
internal fun ByteArray.toBase64String(): String {
    val start = 0
    val len = size
    val buf = StringBuffer(size * 3 / 2)
    val end = len - 3
    var i = start
    var n = 0
    while (i <= end) {
        val d = (this[i].toInt() and 0x0ff shl 16
                or (this[i + 1].toInt() and 0x0ff shl 8)
                or (this[i + 2].toInt() and 0x0ff))
        buf.append(BASE64_ALPHABET[d shr 18 and 0x3f])
        buf.append(BASE64_ALPHABET[d shr 12 and 0x3f])
        buf.append(BASE64_ALPHABET[d shr 6 and 0x3f])
        buf.append(BASE64_ALPHABET[d and 0x3f])
        i += 3
        if (n++ >= 14) {
            n = 0
            buf.append(" ")
        }
    }
    if (i == start + len - 2) {
        val d = (this[i].toInt() and 0x0ff shl 16
                or (this[i + 1].toInt() and 255 shl 8))
        buf.append(BASE64_ALPHABET[d shr 18 and 0x3f])
        buf.append(BASE64_ALPHABET[d shr 12 and 0x3f])
        buf.append(BASE64_ALPHABET[d shr 6 and 0x3f])
        buf.append("=")
    } else if (i == start + len - 1) {
        val d = this[i].toInt() and 0x0ff shl 16
        buf.append(BASE64_ALPHABET[d shr 18 and 0x3f])
        buf.append(BASE64_ALPHABET[d shr 12 and 0x3f])
        buf.append("==")
    }
    return buf.toString()
//    val l = size
//    val sb = StringBuilder((l + 3) * 4 / 3)
//    val mod = l % 3
//    val ll = l - mod
//    var triad: Int
//    var i = 0
//    while (i < ll) {
//        triad = (this[i].toInt() shl 16) or (this[i + 1].toInt() shl 8) or this[i + 2].toInt()
//        sb.append(BASE64_ALPHABET[triad shr 18 and 0x3f])
//        sb.append(BASE64_ALPHABET[triad shr 12 and 0x3f])
//        sb.append(BASE64_ALPHABET[triad shr 6 and 0x3f])
//        sb.append(BASE64_ALPHABET[triad and 0x3f])
//        i += 3
//    }
//    if (mod == 1) {
//        sb.append(BASE64_ALPHABET[this[i].toInt() shr 2 and 0x3f])
//        sb.append(BASE64_ALPHABET[this[i].toInt() shl 4 and 0x3f])
//        sb.append("==")
//    }
//    if (mod == 2) {
//        triad = this[i].toInt() shl 8 or this[i + 1].toInt()
//        sb.append(BASE64_ALPHABET[triad shr 10 and 0x3ff])
//        sb.append(BASE64_ALPHABET[triad shr 4 and 0x3f])
//        sb.append(BASE64_ALPHABET[triad shl 2 and 0x3f])
//        sb.append("=")
//    }
//    return sb.toString()
}