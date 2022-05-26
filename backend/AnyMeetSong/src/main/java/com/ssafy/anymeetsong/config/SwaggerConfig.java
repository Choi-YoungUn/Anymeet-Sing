package com.ssafy.anymeetsong.config;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

// http://localhost:8080/anymeetsong/swagger-ui.html
   
@ConditionalOnExpression(value = "${useSwagger:false}")
@Configuration
@EnableSwagger2
public class SwaggerConfig {

   private String version = "v1.0";
   private String title = "Any Meet & Song " + version;
   
   @Bean
   public Docket postsApi() {
      List<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();
      responseMessages.add(new ResponseMessageBuilder().code(200).message("OK").build());
      responseMessages.add(new ResponseMessageBuilder().code(500).message("서버 문제 발생").responseModel(new ModelRef("Error")).build());
      responseMessages.add(new ResponseMessageBuilder().code(404).message("페이지를 찾을 수 없습니다.").build());
      return new Docket(DocumentationType.SWAGGER_2)
            .groupName("AnyMeetSong")
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.ssafy.anymeetsong.controller"))
            .paths(postPaths())
            .build();
//            .useDefaultResponseMessages(false)
//            .globalResponseMessage(RequestMethod.GET,responseMessages);
   }
   
   private Predicate<String> postPaths() {
      return PathSelectors.any();
//      return PathSelectors.ant("/**")
//      return or(regex("/user/.*"), regex("/article/.*"), regex("/memo/.*"));
//      return regex("/admin/.*");
   }

   private ApiInfo apiInfo() {
      return new ApiInfoBuilder().title(title)
            .description("SSAFY API Reference for Developers")
            .termsOfServiceUrl("https://edu.ssafy.com")
            .contact(new Contact("SSAFY", "https://edu.ssafy.com", "ssafy@ssafy.com"))
            .license("SSAFY License")
            .licenseUrl("ssafy@ssafy.com").version("1.0").build();
   }

}

