package test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import main.java.com.carrental.model.Customer;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.util.JSONUtil;
import main.java.com.carrental.util.SecurePasswordHasher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for utility classes.
 * Uses only JUnit 5. A simple FakeHttpExchange is provided
 * for testing HTTP-related methods.
 */
public class UtilsTest {

    // ------------------------------------------------------------------------
    // Helper: A minimal HttpExchange implementation for testing
    // ------------------------------------------------------------------------
    static class FakeHttpExchange extends HttpExchange {
        private final String requestMethod;
        private final URI requestURI;
        private final InputStream requestBody;
        private final Headers responseHeaders = new Headers();
        private int responseStatusCode;
        private long responseContentLength;
        private final ByteArrayOutputStream responseBodyStream = new ByteArrayOutputStream();

        public FakeHttpExchange(String method, String uri, String requestBodyContent) {
            this.requestMethod = method;
            try {
                this.requestURI = new URI(uri);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            this.requestBody = new ByteArrayInputStream(requestBodyContent.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String getRequestMethod() { return requestMethod; }

        @Override
        public URI getRequestURI() { return requestURI; }

        @Override
        public InputStream getRequestBody() { return requestBody; }

        @Override
        public Headers getResponseHeaders() { return responseHeaders; }

        @Override
        public void sendResponseHeaders(int statusCode, long contentLength) {
            this.responseStatusCode = statusCode;
            this.responseContentLength = contentLength;
        }

        @Override
        public OutputStream getResponseBody() { return responseBodyStream; }

        @Override
        public void close() { /* nothing */ }

        // Unimplemented methods (not needed for tests)
        @Override public Headers getRequestHeaders() { return null; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void setAttribute(String name, Object value) { }
        @Override public Object getAttribute(String name) { return null; }

		@Override
		public InetSocketAddress getRemoteAddress() {
			return null;
		}

		@Override
		public int getResponseCode() {
			return 0;
		}

		@Override
		public InetSocketAddress getLocalAddress() {
			return null;
		}

		@Override
		public String getProtocol() {
			return null;
		}

		@Override
		public void setStreams(InputStream i, OutputStream o) {
			
		}

		@Override
		public HttpPrincipal getPrincipal() {
			// TODO Auto-generated method stub
			return null;
		}
    }

    // ------------------------------------------------------------------------
    // Tests for JSONUtil
    // ------------------------------------------------------------------------
    @Nested
    class JSONUtilTest {

        // ----- jsonifyString -----
        @Test
        void jsonifyString_null_returnsNullLiteral() {
            assertEquals("null", JSONUtil.jsonifyString(null));
        }

        @Test
        void jsonifyString_emptyString_returnsQuotes() {
            assertEquals("\"\"", JSONUtil.jsonifyString(""));
        }

        @Test
        void jsonifyString_plainText_noEscapes() {
            assertEquals("\"Hello World\"", JSONUtil.jsonifyString("Hello World"));
        }

        @Test
        void jsonifyString_specialCharacters_escapesCorrectly() {
            String input = "He said \"Hello\" and then \\ escaped.\nNew line.";
            String expected = "\"He said \\\"Hello\\\" and then \\\\ escaped.\\nNew line.\"";
            assertEquals(expected, JSONUtil.jsonifyString(input));
        }

        @Test
        void jsonifyString_controlCharacters_escapesAsUnicode() {
            // Characters below 0x20 should be \ uXXXX
            String input = "a\u0001b\u0002c";
            // \u0001 and \u0002 are control characters
            String expected = "\"a\\u0001b\\u0002c\"";
            assertEquals(expected, JSONUtil.jsonifyString(input));
        }

        @Test
        void jsonifyString_unicodeCharacters_preserved() {
            String input = "Café 测试";
            String expected = "\"Café 测试\"";
            assertEquals(expected, JSONUtil.jsonifyString(input));
        }

        // ----- readBody -----
        @Test
        void readBody_normalJson_returnsString() {
            String jsonBody = "{\"name\":\"John\",\"email\":\"john@test.com\"}";
            FakeHttpExchange exchange = new FakeHttpExchange("POST", "/test", jsonBody);
            String result = JSONUtil.readBody(exchange);
            assertEquals(jsonBody, result);
        }

        @Test
        void readBody_emptyBody_returnsEmptyString() {
            FakeHttpExchange exchange = new FakeHttpExchange("POST", "/test", "");
            String result = JSONUtil.readBody(exchange);
            assertEquals("", result);
        }

        // ----- sendResponse -----
        @Test
        void sendResponse_setsHeadersAndBody() throws IOException {
            FakeHttpExchange exchange = new FakeHttpExchange("POST", "/test", "");
            String responseBody = "{\"status\":\"ok\"}";
            int statusCode = 201;

            JSONUtil.sendResponse(exchange, statusCode, responseBody);

            assertEquals(statusCode, exchange.responseStatusCode);
            assertEquals("application/json", exchange.responseHeaders.getFirst("Content-Type"));
            assertEquals(responseBody, exchange.responseBodyStream.toString(StandardCharsets.UTF_8));
        }

        // ----- extractField -----
        @Test
        void extractField_existingField_returnsValue() {
            String json = "{\"name\":\"John\", \"age\":30}";
            assertEquals("John", JSONUtil.extractField(json, "name"));
        }

        @Test
        void extractField_missingField_returnsNull() {
            String json = "{\"name\":\"John\"}";
            assertNull(JSONUtil.extractField(json, "age"));
        }

        @Test
        void extractField_fieldWithEscapedQuotes_returnsCorrectly() {
            // Our simple regex doesn't handle escaped quotes, but this test shows current behavior.
            String json = "{\"name\":\"John \\\"Doe\\\"\"}";
            // The regex will stop at the first unescaped quote after the opening quote,
            // so it will return "John \\" – not ideal, but the method is simple.
            // This test documents that behavior; if improved later, update test.
            assertEquals("John \\", JSONUtil.extractField(json, "name"));
        }

        // ----- parseCustomerFromJson -----
        @Test
        void parseCustomerFromJson_validJson_returnsCustomer() {
            String json = "{\"name\":\"Jane Doe\",\"email\":\"jane@test.com\",\"password\":\"secret123\"}";
            Customer customer = JSONUtil.parseCustomerFromJson(json);

            assertNotNull(customer);
            assertEquals("Jane Doe", customer.getCustomerName());
            assertEquals("jane@test.com", customer.getEmail());
            // Password should be hashed, not the plain text
            assertNotEquals("secret123", customer.getPassword());
            assertTrue(customer.getPassword().contains(":")); // typical hash format
        }

        @Test
        void parseCustomerFromJson_missingField_returnsNull() {
            String json = "{\"name\":\"Jane Doe\",\"email\":\"jane@test.com\"}"; // no password
            assertNull(JSONUtil.parseCustomerFromJson(json));

            json = "{\"name\":\"Jane Doe\",\"password\":\"secret\"}"; // no email
            assertNull(JSONUtil.parseCustomerFromJson(json));

            json = "{\"email\":\"jane@test.com\",\"password\":\"secret\"}"; // no name
            assertNull(JSONUtil.parseCustomerFromJson(json));
        }


        // ----- escapeJson -----
        @Test
        void escapeJson_null_returnsEmpty() {
            assertEquals("", JSONUtil.escapeJson(null));
        }

        @Test
        void escapeJson_plainText_unchanged() {
            assertEquals("Hello", JSONUtil.escapeJson("Hello"));
        }

        @Test
        void escapeJson_backslashAndQuotes_escaped() {
            String input = "\\ and \"quoted\"";
            String expected = "\\\\ and \\\"quoted\\\"";
            assertEquals(expected, JSONUtil.escapeJson(input));
        }
        @Test
        void testRentalsToJson() {
            List<Rental> rentals = new ArrayList<>();
            Rental r1 = new Rental();
            r1.setRentalID("1");
            Vehicle v1 = new Vehicle();
            v1.setMake("Toyota");
            v1.setModel("Camry");
            v1.setYear(2022);
            r1.setVehicle(v1);
            r1.setPickupDate(LocalDate.of(2026,1,10));
            r1.setPlannedReturnDate(LocalDate.of(2026,1,15));
            r1.setTotalCost(225.0);
            rentals.add(r1);
            String json = JSONUtil.RentalsToJson(rentals);
            assertTrue(json.contains("\"id\":\"1\""));
            assertTrue(json.contains("\"carName\":\"Toyota Camry (2022)\""));
            assertTrue(json.contains("\"start\":\"2026-01-10\""));
            assertTrue(json.contains("\"end\":\"2026-01-15\""));
            assertTrue(json.contains("\"total\":\"225.0\""));
        }

        @Test
        void testVehiclesToJson() {
            List<Vehicle> vehicles = new ArrayList<>();
            Vehicle v = new Vehicle();
            v.setId("10");
            v.setMake("Honda");
            v.setModel("Civic");
            v.setYear(2023);
            v.setDailyRate(50.0);
            v.setType(Vehicle.VehicleType.SEDAN);
            v.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicles.add(v);
            String json = JSONUtil.VehiclesToJson(vehicles);
            assertTrue(json.contains("\"id\":\"10\""));
            assertTrue(json.contains("\"make\":\"Honda\""));
            assertTrue(json.contains("\"model\":\"Civic\""));
            assertTrue(json.contains("\"year\":2023"));
            assertTrue(json.contains("\"dailyrate\":50.0"));
            assertTrue(json.contains("\"type\":\"SEDAN\""));
            assertTrue(json.contains("\"status\":\"AVAILABLE\""));
        }

        @Test
        void testVehicleToJson() {
            Vehicle v = new Vehicle();
            v.setId("5");
            v.setMake("Ford");
            v.setModel("F-150");
            v.setYear(2021);
            v.setDailyRate(95.0);
            v.setType(Vehicle.VehicleType.TRUCK);
            v.setStatus(Vehicle.VehicleStatus.RENTED);
            String json = JSONUtil.VehicleToJson(v);
            assertTrue(json.contains("\"id\":\"5\""));
            assertTrue(json.contains("\"make\":\"Ford\""));
            assertTrue(json.contains("\"model\":\"F-150\""));
            assertTrue(json.contains("\"year\":2021"));
            assertTrue(json.contains("\"dailyrate\":95.0"));
            assertTrue(json.contains("\"type\":\"TRUCK\""));
            assertTrue(json.contains("\"status\":\"RENTED\""));
        }

        @Test
        void testMapToJson() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "Alice");
            map.put("age", 30);
            map.put("active", true);
            String json = JSONUtil.mapToJson(map);
            assertTrue(json.contains("\"name\":\"Alice\""));
            assertTrue(json.contains("\"age\":30"));
            assertTrue(json.contains("\"active\":true"));
        }

        @Test
        void testListMapToJson() {
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> m1 = new HashMap<>();
            m1.put("id", 1);
            Map<String, Object> m2 = new HashMap<>();
            m2.put("id", 2);
            list.add(m1);
            list.add(m2);
            String json = JSONUtil.listMapToJson(list);
            assertEquals("[{\"id\":1},{\"id\":2}]", json);
        }
    }

    // ------------------------------------------------------------------------
    // Tests for SecurePasswordHasher
    // ------------------------------------------------------------------------
    @Nested
    class SecurePasswordHasherTest {

        @Test
        void hashPassword_returnsNonNullAndFormatted() {
            String hash = SecurePasswordHasher.hashPassword("testPassword");
            assertNotNull(hash);
            String[] parts = hash.split(":");
            assertEquals(4, parts.length);
            assertAll(
                () -> assertFalse(parts[0].isEmpty()), // salt base64
                () -> assertFalse(parts[1].isEmpty()), // hash base64
                () -> assertTrue(Integer.parseInt(parts[2]) > 0), // iterations
                () -> assertTrue(Integer.parseInt(parts[3]) > 0)  // key length
            );
        }

        @Test
        void hashPassword_samePassword_differentHashes() {
            String hash1 = SecurePasswordHasher.hashPassword("same");
            String hash2 = SecurePasswordHasher.hashPassword("same");
            // Salts are random, so the full strings should differ
            assertNotEquals(hash1, hash2);
        }

        @Test
        void verifyPassword_correctPassword_returnsTrue() {
            String password = "mySecret123";
            String storedHash = SecurePasswordHasher.hashPassword(password);
            assertTrue(SecurePasswordHasher.verifyPassword(password, storedHash));
        }

        @Test
        void verifyPassword_incorrectPassword_returnsFalse() {
            String password = "mySecret123";
            String storedHash = SecurePasswordHasher.hashPassword(password);
            assertFalse(SecurePasswordHasher.verifyPassword("wrongPassword", storedHash));
        }

        @Test
        void verifyPassword_malformedHash_returnsFalse() {
            // Should not throw exception; returns false (MessageDigest.isEqual with null? careful)
            // The method currently uses MessageDigest.isEqual(computedHash, storedHash).
            // If computedHash is null (due to exception) it will compare null with storedHash -> false.
            String malformed = "invalid:format:1:3";
            assertFalse(SecurePasswordHasher.verifyPassword("any", malformed));
        }

        @Test
        void verifyPassword_invalidBase64_returnsFalse() {
            // parts[0] is not valid Base64; decoding will throw IllegalArgumentException,
            // but the method does not catch it. The exception will propagate.
            // However, the method's catch blocks only catch NoSuchAlgorithmException and InvalidKeySpecException,
            // so IllegalArgumentException from Base64.getDecoder().decode will propagate and likely fail the test.
            // This is a known issue; we document it here.
            String invalidBase64Hash = "not-base64:hash:10000:256";
            assertThrows(IllegalArgumentException.class, () -> {
                SecurePasswordHasher.verifyPassword("pwd", invalidBase64Hash);
            });
            // If you want to test the method's resilience, you'd need to catch that exception.
            // For now, we note the behavior.
        }

        @Test
        void hashPassword_nullInput_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () -> {
                SecurePasswordHasher.hashPassword(null);
            });
        }

        @Test
        void verifyPassword_nullInput_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () -> {
                SecurePasswordHasher.verifyPassword(null, "some:hash:1:1");
            });
            assertThrows(NullPointerException.class, () -> {
                SecurePasswordHasher.verifyPassword("pwd", null);
            });
        }
    }
}
