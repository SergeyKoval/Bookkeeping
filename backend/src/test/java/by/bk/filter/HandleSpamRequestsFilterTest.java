package by.bk.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test that verifies HandleSpamRequestsFilter properly rejects spam requests.
 *
 * @author Sergey Koval
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
    HandleSpamRequestsFilterTest.TestConfig.class,
    HandleSpamRequestsFilter.class
})
class HandleSpamRequestsFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectMultipartRequests() throws Exception {
        // Multipart requests should be rejected with 415 Unsupported Media Type
        // since this API only accepts JSON
        mockMvc.perform(post("/test/endpoint")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .content("--boundary\r\nContent-Disposition: form-data; name=\"file\"\r\n\r\ntest\r\n--boundary--"))
            .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldRejectInvalidAcceptHeader() throws Exception {
        // Requests with invalid Accept headers should be rejected
        mockMvc.perform(post("/test/endpoint")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept", "invalid/mime/type/with/extra/slashes")
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowValidJsonRequests() throws Exception {
        // Valid JSON requests should pass through
        mockMvc.perform(post("/test/endpoint")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
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
        @PostMapping("/test/endpoint")
        public String testEndpoint() {
            return "ok";
        }
    }
}
