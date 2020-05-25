package http

data class Headers(
        private val map: MutableMap<String, MutableList<String>>
) : MutableMap<String, MutableList<String>> by map {

    constructor(raw: String) : this(mutableMapOf()) {
        TODO("to be finished.")
    }

    /**
     * Get the first value of the header. Useful for headers that only
     * have one value, or are expected to only have one value.
     * @return the first value of header, if header doesn't exist this will return an empty String.
     */
    fun getFirst(key: String): String = this[key]?.get(0) ?: ""

    /**
     * Get the first value of the header. Useful for headers that only
     * have one value, or are expected to only have one value.
     * @return the first value of header, if header doesn't exist this will return null.
     */
    fun getFirstOrNull(key: String): String? = this[key]?.get(0)

    /**
     * Add a value to a header. Appends to existing header values if exists, otherwise
     * this function will initialize a list with the provided value.
     */
    fun add(key: String, value: String) {
        this[key] = this[key]?.apply {
            add(value)
        } ?: mutableListOf(value)
    }
}