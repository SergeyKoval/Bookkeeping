package by.bk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Sergey Koval
 */
@SpringBootApplication
@EnableMongoRepositories
public class Bookkeeper extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Bookkeeper.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Bookkeeper.class, args);
    }
}