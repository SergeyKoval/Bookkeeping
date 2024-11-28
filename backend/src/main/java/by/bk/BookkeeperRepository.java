package by.bk;

import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author Sergey Koval
 */
@Configuration
@EnableMongoRepositories
public class BookkeeperRepository {
    @Autowired
    private MongoDatabaseFactory mongoDbFactory;
    @Autowired
    private MongoMappingContext mongoMappingContext;

    @Bean
    public MongoTemplate mongoTemplate() {
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory), mongoMappingContext);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        converter.setCustomConversions(new MongoCustomConversions(Arrays.asList(
                DoubleToDecimal128Converter.INSTANCE,
                Decimal128ToDoubleConverter.INSTANCE
        )));
        converter.afterPropertiesSet();

        return new MongoTemplate(mongoDbFactory, converter);
    }

    public enum DoubleToDecimal128Converter implements Converter<Double, Decimal128> {
        INSTANCE;
        @Override
        public Decimal128 convert(Double source) {
            return new Decimal128(BigDecimal.valueOf(source));
        }
    }

    public enum Decimal128ToDoubleConverter implements Converter<Decimal128, Double> {
        INSTANCE;
        @Override
        public Double convert(Decimal128 source) {
            return source.bigDecimalValue().doubleValue();
        }
    }
}
