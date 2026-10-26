package demo.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JacksonConfiguration {
    @Bean
    ObjectMapper geminiObjectMapper() {
        return new ObjectMapper();
    }
}
