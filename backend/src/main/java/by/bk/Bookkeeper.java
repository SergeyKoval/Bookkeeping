package by.bk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author Sergey Koval
 */
@SpringBootApplication
public class Bookkeeper extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Bookkeeper.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Bookkeeper.class, args);
    }
}