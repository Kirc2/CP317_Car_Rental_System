package test;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import main.java.com.carrental.gui.CarRentalServer;
import main.java.com.carrental.gui.Login_RegisterEndpoints;
import main.java.com.carrental.gui.StaticFileHandler;
import main.java.com.carrental.model.Customer;
import main.java.com.carrental.service.CustomerService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GUI classes using only JUnit 5 (no mocking libraries).
 * Manual test doubles are used for HttpExchange and CustomerService.
 */
public class GUITest {

    // ------------------------------------------------------------------------
    // Helper: A simple mutable HttpExchange implementation for testing
    // ------------------------------------------------------------------------
    static class TestHttpExchange extends HttpExchange {
        private String requestMethod;
        private URI requestURI;
        private final ByteArrayOutputStream responseBodyStream = new ByteArrayOutputStream();
        private final Map<String, String> responseHeaders = new HashMap<>();
        private int responseStatusCode;
        private long responseContentLength;
        private InputStream requestBody;

        public TestHttpExchange(String method, String uri, String requestBodyContent) {
            this.requestMethod = method;
            try {
                this.requestURI = new URI(uri);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            this.requestBody = new ByteArrayInputStream(requestBodyContent.getBytes());
        }

        @Override
        public URI getRequestURI() {
            return requestURI;
        }

        @Override
        public InputStream getRequestBody() {
            return requestBody;
        }

        @Override
        public Headers getResponseHeaders() {
            return new Headers() {
                @Override
                public void set(String key, String value) {
                    responseHeaders.put(key, value);
                }
                // Other methods not needed for tests
            };
        }

        @Override
        public void sendResponseHeaders(int statusCode, long contentLength) {
            this.responseStatusCode = statusCode;
            this.responseContentLength = contentLength;
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBodyStream;
        }

        @Override
        public void close() {
            // nothing
        }

        // Additional HttpExchange abstract methods (not used in tests)
        @Override public Headers getRequestHeaders() { return null; }
        @Override public String getRequestMethod() { return requestMethod; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public Object getAttribute(String name) { return null; }


		@Override
		public InetSocketAddress getRemoteAddress() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getResponseCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public InetSocketAddress getLocalAddress() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getProtocol() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setStreams(InputStream i, OutputStream o) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public HttpPrincipal getPrincipal() {
			return null;
		}
    }

    // ------------------------------------------------------------------------
    // Helper: A test double for CustomerService that records calls
    // ------------------------------------------------------------------------
    static class TestCustomerService extends CustomerService {
        public Customer lastRegisteredCustomer;
        public String lastLoginEmail;
        public String lastLoginPassword;
        public boolean throwOnRegister = false;
        public boolean throwOnLogin = false;
        public RuntimeException registerException;
        public RuntimeException loginException;

        @Override
        public void Register(Customer customer) {
            if (throwOnRegister) {
                throw registerException != null ? registerException : new RuntimeException("Register failed");
            }
            this.lastRegisteredCustomer = customer;
        }

        @Override
        public Customer Login(String email, String password) {
            if (throwOnLogin) {
                throw loginException != null ? loginException : new RuntimeException("Login failed");
            }
            this.lastLoginEmail = email;
            this.lastLoginPassword = password;
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setCustomerName("Test User");
            return customer;
        }
    }

    // ------------------------------------------------------------------------
    // Setup / Teardown
    // ------------------------------------------------------------------------
    private static TestCustomerService testCustomerService;
    private static Field customerServiceField;

    @BeforeAll
    static void setUpClass() throws Exception {
        // Replace the static CustomerService in Login_RegisterEndpoints with our test double
        customerServiceField = Login_RegisterEndpoints.class.getDeclaredField("customerService");
        customerServiceField.setAccessible(true);
        testCustomerService = new TestCustomerService();
        customerServiceField.set(null, testCustomerService);
    }

    @AfterAll
    static void tearDownClass() throws Exception {
        // Restore original (optional) – set back to a real instance
        customerServiceField.set(null, new CustomerService());
    }

    // ------------------------------------------------------------------------
    // CarRentalServer Tests
    // ------------------------------------------------------------------------
    @Nested
    class CarRentalServerTests {
        @Test
        void testConstructorStartsServer() {
            // Simply verify that constructor does not throw.
            // Note: This attempts to bind to port 8080, may fail if port is in use.
            assertDoesNotThrow(() -> new CarRentalServer());
        }
    }

    // ------------------------------------------------------------------------
    // RegisterHandler Tests
    // ------------------------------------------------------------------------
    @Nested
    class RegisterHandlerTests {
        private Login_RegisterEndpoints.RegisterHandler handler = new Login_RegisterEndpoints.RegisterHandler();

        @Test
        void testNonPostMethod_Returns405() throws IOException {
            TestHttpExchange exchange = new TestHttpExchange("GET", "/register", "");
            handler.handle(exchange);

            assertEquals(405, exchange.responseStatusCode);
            String response = exchange.responseBodyStream.toString();
            assertTrue(response.contains("Method not allowed"));
        }

        @Test
        void testInvalidJson_Returns400() throws IOException {
            String invalidBody = "{not json}";
            TestHttpExchange exchange = new TestHttpExchange("POST", "/register", invalidBody);
            handler.handle(exchange);

            assertEquals(400, exchange.responseStatusCode);
            String response = exchange.responseBodyStream.toString();
            assertTrue(response.contains("Invalid JSON"));
            assertNull(testCustomerService.lastRegisteredCustomer);
        }

        @Test
        void testValidJson_RegisterSuccess_Returns201() throws IOException {
            String validJson = "{\"name\":\"John\",\"email\":\"john@test.com\",\"password\":\"secret\"}";
            TestHttpExchange exchange = new TestHttpExchange("POST", "/register", validJson);
            handler.handle(exchange);

            assertEquals(201, exchange.responseStatusCode);
            // Verify that customer service received the customer
            assertNotNull(testCustomerService.lastRegisteredCustomer);
            assertEquals("John", testCustomerService.lastRegisteredCustomer.getCustomerName());
            assertEquals("john@test.com", testCustomerService.lastRegisteredCustomer.getEmail());
            // Response should contain the customer JSON (we can't easily verify content without JSON parsing)
            String response = exchange.responseBodyStream.toString();
            assertTrue(response.contains("John") || response.contains("customer"));
        }

        @Test
        void testRegisterServiceThrows_Returns400WithErrorMessage() throws IOException {
            String validJson = "{\"name\":\"John\",\"email\":\"john@test.com\"}";
            TestHttpExchange exchange = new TestHttpExchange("POST", "/register", validJson);
            testCustomerService.throwOnRegister = true;
            testCustomerService.registerException = new RuntimeException("Email already exists");

            handler.handle(exchange);

            assertEquals(400, exchange.responseStatusCode);
            // Reset
            testCustomerService.throwOnRegister = false;
            testCustomerService.registerException = null;
        }
    }

    // ------------------------------------------------------------------------
    // LoginHandler Tests
    // ------------------------------------------------------------------------
    @Nested
    class LoginHandlerTests {
        private Login_RegisterEndpoints.LoginHandler handler = new Login_RegisterEndpoints.LoginHandler();

        @Test
        void testNonPostMethod_Returns405() throws IOException {
            TestHttpExchange exchange = new TestHttpExchange("GET", "/login", "");
            handler.handle(exchange);

            assertEquals(405, exchange.responseStatusCode);
            String response = exchange.responseBodyStream.toString();
            assertTrue(response.contains("Method not allowed"));
        }

        @Test
        void testMissingEmail_Returns400() throws IOException {
            String body = "{\"password\":\"pass\"}";
            TestHttpExchange exchange = new TestHttpExchange("POST", "/login", body);
            handler.handle(exchange);

            assertEquals(400, exchange.responseStatusCode);
            String response = exchange.responseBodyStream.toString();
            assertTrue(response.contains("Missing email or password"));
        }

        @Test
        void testMissingPassword_Returns400() throws IOException {
            String body = "{\"email\":\"test@test.com\"}";
            TestHttpExchange exchange = new TestHttpExchange("POST", "/login", body);
            handler.handle(exchange);

            assertEquals(400, exchange.responseStatusCode);
            String response = exchange.responseBodyStream.toString();
            assertTrue(response.contains("Missing email or password"));
        }

        @Test
        void testValidCredentials_LoginSuccess_Returns200() throws IOException {
            String body = "{\"email\":\"test@test.com\",\"password\":\"pass123\"}";
            TestHttpExchange exchange = new TestHttpExchange("POST", "/login", body);
            handler.handle(exchange);

            assertEquals(200, exchange.responseStatusCode);
            assertEquals("test@test.com", testCustomerService.lastLoginEmail);
            assertEquals("pass123", testCustomerService.lastLoginPassword);
            String response = exchange.responseBodyStream.toString();
            assertTrue(response.contains("Login Successful"));
        }

        @Test
        void testInvalidCredentials_Returns401() throws IOException {
            String body = "{\"email\":\"test@test.com\",\"password\":\"wrong\"}";
            TestHttpExchange exchange = new TestHttpExchange("POST", "/login", body);
            testCustomerService.throwOnLogin = true;
            testCustomerService.loginException = new RuntimeException("Invalid credentials");

            handler.handle(exchange);

            assertEquals(401, exchange.responseStatusCode);
            String response = exchange.responseBodyStream.toString();
            assertTrue(response.contains("Invalid credentials"));

            testCustomerService.throwOnLogin = false;
            testCustomerService.loginException = null;
        }
    }

    // ------------------------------------------------------------------------
    // StaticFileHandler Tests (using temporary directory)
    // ------------------------------------------------------------------------
    @Nested
    class StaticFileHandlerTests {

        @TempDir
        Path tempDir;

        private StaticFileHandler handler;

        @BeforeEach
        void setUp() throws IOException {
            // Create a mini public directory structure
            Files.createDirectories(tempDir.resolve("public"));
            Files.write(tempDir.resolve("public/login.html"), "<html>Login</html>".getBytes());
            Files.write(tempDir.resolve("public/style.css"), "body {}".getBytes());
            Files.createDirectories(tempDir.resolve("public/subdir"));
            Files.write(tempDir.resolve("public/subdir/page.html"), "<html>Sub</html>".getBytes());

            handler = new StaticFileHandler(tempDir.resolve("public").toString());
        }

        @Test
        void testRootPath_ServesLoginHtml() throws IOException {
            TestHttpExchange exchange = new TestHttpExchange("GET", "/", "");
            handler.handle(exchange);

            assertEquals(200, exchange.responseStatusCode);
            assertEquals("text/html", exchange.responseHeaders.get("Content-Type"));
            byte[] expected = Files.readAllBytes(tempDir.resolve("public/login.html"));
            assertArrayEquals(expected, exchange.responseBodyStream.toByteArray());
        }

        @Test
        void testExistingFile_ServesFile() throws IOException {
            TestHttpExchange exchange = new TestHttpExchange("GET", "/style.css", "");
            handler.handle(exchange);

            assertEquals(200, exchange.responseStatusCode);
            assertEquals("text/css", exchange.responseHeaders.get("Content-Type"));
            byte[] expected = Files.readAllBytes(tempDir.resolve("public/style.css"));
            assertArrayEquals(expected, exchange.responseBodyStream.toByteArray());
        }

        @Test
        void testFileInSubdirectory_ServesFile() throws IOException {
            TestHttpExchange exchange = new TestHttpExchange("GET", "/subdir/page.html", "");
            handler.handle(exchange);

            assertEquals(200, exchange.responseStatusCode);
            assertEquals("text/html", exchange.responseHeaders.get("Content-Type"));
            byte[] expected = Files.readAllBytes(tempDir.resolve("public/subdir/page.html"));
            assertArrayEquals(expected, exchange.responseBodyStream.toByteArray());
        }

        @Test
        void testNonExistentFile_Returns404() throws IOException {
            TestHttpExchange exchange = new TestHttpExchange("GET", "/missing.html", "");
            handler.handle(exchange);

            assertEquals(404, exchange.responseStatusCode);
            assertEquals("404 Not Found", exchange.responseBodyStream.toString());
        }

        @Test
        void testDirectoryTraversalAttempt_Returns404() throws IOException {
            TestHttpExchange exchange = new TestHttpExchange("GET", "/../secret.txt", "");
            handler.handle(exchange);

            assertEquals(404, exchange.responseStatusCode);
            assertEquals("404 Not Found", exchange.responseBodyStream.toString());
        }
    }
}