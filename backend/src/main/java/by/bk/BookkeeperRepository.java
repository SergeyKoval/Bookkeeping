package by.bk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Sergey Koval
 */
@Configuration
@EnableMongoRepositories
public class BookkeeperRepository {
    @Autowired
    private MongoDbFactory mongoDbFactory;
    @Autowired
    private MongoMappingContext mongoMappingContext;

    @Bean
    public MongoTemplate mongoTemplate() {
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory), mongoMappingContext);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return new MongoTemplate(mongoDbFactory, converter);
    }
}