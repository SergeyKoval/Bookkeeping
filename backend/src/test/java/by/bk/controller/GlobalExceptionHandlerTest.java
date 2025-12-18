package by.bk.controller;

import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test that verifies GlobalExceptionHandler properly catches and handles
 * client disconnection exceptions without propagating them as errors.
 *
 * @author Sergey Koval
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
    GlobalExceptionHandlerTest.TestConfig.class,
    GlobalExceptionHandler.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldHandleAsyncRequestNotUsableException() throws Exception {
        // When endpoint throws AsyncRequestNotUsableException
        // Then it should be handled gracefully without propagating error
        mockMvc.perform(get("/test/async-not-usable"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldHandleClientAbortException() throws Exception {
        // When endpoint throws ClientAbortException
        // Then it should be handled gracefully without propagating error
        mockMvc.perform(get("/test/client-abort"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldHandleMultipartException() throws Exception {
        // When endpoint throws MultipartException (e.g., from malformed multipart request)
        // Then it should return 400 Bad Request without logging stack trace
        mockMvc.perform(get("/test/multipart-error"))
            .andExpect(status().isBadRequest());
    }

    @Configuration
    static class TestConfig {
        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/test/async-not-usable")
        public String throwAsyncNotUsable() throws AsyncRequestNotUsableException {
            throw new AsyncRequestNotUsableException("Test client disconnection");
        }

        @GetMapping("/test/client-abort")
        public String throwClientAbort() throws IOException {
            throw new ClientAbortException(new IOException("Broken pipe"));
        }

        @GetMapping("/test/multipart-error")
        public String throwMultipartException() {
            throw new MultipartException("Stream ended unexpectedly");
        }
    }
}
