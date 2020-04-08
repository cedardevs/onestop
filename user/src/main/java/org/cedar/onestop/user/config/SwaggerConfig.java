package org.cedar.onestop.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ApiInfoBuilder;

//public class SwaggerConfig extends WebMvcConfigurationSupport {
//
//
//
//}
@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build()
        .pathMapping("/")
        .apiInfo(metaData());
  }

  @Override
  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  private ApiInfo metaData() {
    return new ApiInfoBuilder()
        .title("SAVE SEARCH API")
        .description("Spring Boot REST API for User save searches Microservice")
        .version("1.0.0")
        .license("")
        .licenseUrl("")
        .contact(new Contact("cedardevs",
            "https://github.com/cedardevs",
            "cedar.cires@colorado.edu "))
        .build();
  }
}
