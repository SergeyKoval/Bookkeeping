package by.bk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.util.ErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

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
    @Autowired
    private ErrorHandler errorHandler;
    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ConcurrentTaskScheduler scheduler = new ConcurrentTaskScheduler();
        scheduler.setErrorHandler(errorHandler);
        return scheduler;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Bookkeeper.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Bookkeeper.class, args);
    }
}
