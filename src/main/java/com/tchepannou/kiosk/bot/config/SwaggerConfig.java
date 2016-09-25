package com.tchepannou.kiosk.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Value("${swagger.service.version}")
    private String serviceVersion;

    @Value("${swagger.service.title}")
    private String serviceTitle;

    @Value("${swagger.service.description}")
    private String serviceDescription;

    @Value("${swagger.service.termsPath}")
    private String serviceTermsPath;

    @Value("${swagger.service.email}")
    private String serviceEmail;

    @Value("${swagger.service.licenceType}")
    private String serviceLicenceType;

    @Value("${swagger.service.licencePath}")
    private String serviceLicencePath;


    @Bean
    public Docket documentation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(or(regex("/kiosk/.*?")))
                .build()
                .pathMapping("/")
                .apiInfo(apiInfo());
    }

    @Bean
    public UiConfiguration uiConfig() {
        return UiConfiguration.DEFAULT;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(serviceTitle)
                .description(serviceDescription)
                .termsOfServiceUrl(serviceTermsPath)
                .contact(serviceEmail)
                .license(serviceLicenceType)
                .licenseUrl(serviceLicencePath)
                .version(serviceVersion)
                .build();
    }
}

