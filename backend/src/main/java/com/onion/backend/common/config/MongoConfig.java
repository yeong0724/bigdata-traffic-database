package com.onion.backend.common.config;

import com.onion.backend.common.utils.DateToLocalDateTimeKstConverter;
import com.onion.backend.common.utils.LocalDateTimeToDateKstConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class MongoConfig {
    @Bean
    public MongoCustomConversions customConversions(
            LocalDateTimeToDateKstConverter localDateTimeToDateKstConverter,
            DateToLocalDateTimeKstConverter dateToLocalDateTimeKstConverter
    ) {
        return new MongoCustomConversions(Arrays.asList(
                localDateTimeToDateKstConverter,
                dateToLocalDateTimeKstConverter
        ));
    }
}
