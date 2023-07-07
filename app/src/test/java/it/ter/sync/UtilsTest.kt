import it.ter.sync.utils.Utils
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {

    @Test
    fun testCalculateDistance() {
        // Coordinate Roma
        val lat1 = 41.9028
        val lon1 = 12.4964

        // Coordinate Milano
        val lat2 = 45.4642
        val lon2 = 9.1900

        // Distanza approssimativa tra Roma e Milano in chilometri
        val expectedDistance = 476.0

        val calculatedDistance = Utils.calculateDistance(lat1, lon1, lat2, lon2)

        assertEquals(expectedDistance, calculatedDistance, 1.0)
    }

    @Test
    fun testGenerateChatId() {
        val string1 = "user1"
        val string2 = "user2"

        val expectedChatId = "user1user2"

        val generatedChatId = Utils.generateChatId(string1, string2)

        assertEquals(expectedChatId, generatedChatId)
    }


    @Test
    fun testCalculateAge() {
        val dateOfBirth = "10/01/1990"
        val expectedAge = 33

        val calculatedAge = Utils.calculateAge(dateOfBirth)

        assertEquals(expectedAge, calculatedAge)
    }
}

