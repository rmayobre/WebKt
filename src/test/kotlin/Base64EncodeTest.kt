import org.junit.Assert
import org.junit.Test
import websocket.toBase64String
import java.util.*

class Base64EncodeTest {

    @Test
    fun encodeTest() {
        val str = "This is a test string."

        val javaBase64 = Base64.getEncoder().encodeToString(str.toByteArray())
        val customeBase64 = str.toByteArray().toBase64String()

        Assert.assertEquals(javaBase64, customeBase64)
    }
}