package com.greenhouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.DomainClassConverter;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Created by niki.max on 28.10.2016.
 */
@EnableWebMvc
@Configuration
@EnableSpringDataWebSupport
@ComponentScan(basePackageClasses = {MvcConfig.class})
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private FormattingConversionService mvcConversionService;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public DomainClassConverter<?> domainClassConverter() {
        return new DomainClassConverter<FormattingConversionService>(mvcConversionService);
    }

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Refresh object mapper!!!
     * Register custom JSON ObjectMapper for Spring MVC.
     * NOTE: using for custom JSON time object mapper!!!
     * {@link JsonConfig}
     *
     * @param converters list of actual MVC message converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
    }
}
