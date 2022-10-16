package com.liva.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 自定义swagger接口文档的配置
 * @author liva
 */

//表示这个类是一个配置类,会把这个类注入到ioc容器中
@Configuration
//开启swagger2的功能
@EnableSwagger2
@Profile({"dev","test"})  //在哪个环境才能生效该配置类
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //这里一定要标注你控制器的位置
                .apis(RequestHandlerSelectors.basePackage("com.liva.usercenter.controller"))
                .paths(PathSelectors.any())
                .build();
    }
    /**
     * api 信息
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("用户中心")
                .description("接口文档")
                .termsOfServiceUrl("https://github.com/liva")
                .contact(new Contact("liva","https://github.com/liva","8622222222@qq.com"))
                .version("1.0")
                .build();
    }
}