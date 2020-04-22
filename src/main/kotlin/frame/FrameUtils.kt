package frame

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
