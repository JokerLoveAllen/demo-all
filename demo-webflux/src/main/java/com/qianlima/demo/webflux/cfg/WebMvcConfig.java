package com.qianlima.demo.webflux.cfg;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;


@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/").setCachePeriod(0);
//        registry.addResourceHandler("/plugin/**", "/static/**")
//                .addResourceLocations("classpath:/plugin/", "classpath:/static/");
        registry.addResourceHandler("/ftl/**").addResourceLocations("classpath:/ftl/");
        registry.addResourceHandler("/plugin/**").addResourceLocations("classpath:/plugin/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        super.addResourceHandlers(registry);
    }
}
