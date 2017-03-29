package com.greenhouse;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * customize global json converters
 * http://docs.spring.io/spring-boot/docs/current/reference/html/howto-spring-mvc.html#howto-customize-the-jackson-objectmapper
 * <p>
 * Created by niki.max on 28.03.2017.
 */
@Configuration
public class JsonConfig implements Jackson2ObjectMapperBuilderCustomizer {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE;

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        jacksonObjectMapperBuilder.serializerByType(LocalDate.class, new CustomLocalDateSerializer());
        jacksonObjectMapperBuilder.deserializerByType(LocalDate.class, new CustomLocalDateDeserializer());
    }

    /**
     * class for custom json date serialization
     */
    private class CustomLocalDateSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(FORMATTER));
        }
    }

    /**
     * class for custom json date deserialization
     */
    private class CustomLocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return LocalDate.parse(p.getValueAsString(), FORMATTER);
        }
    }
}
