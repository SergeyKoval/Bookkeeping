package by.bk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * @author Sergey Koval
 */
@SpringBootApplication
@EnableScheduling
@PropertySources({
        @PropertySource(value = "classpath:application.properties"),
        @PropertySource(value = "file:c:/Users/skoval/Dropbox/bookkeeper/local.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:/opt/bk/server.properties", ignoreResourceNotFound = true)
})
public class Bookkeeper extends SpringBootServletInitializer {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Bookkeeper.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Bookkeeper.class, args);
    }
}