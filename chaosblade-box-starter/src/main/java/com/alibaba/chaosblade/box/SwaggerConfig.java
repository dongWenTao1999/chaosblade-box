package com.alibaba.chaosblade.box;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2//开启Swagger
@EnableSwaggerBootstrapUI
public class SwaggerConfig {

    //配置swagger2核心配置
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2) //指定api类型位swagger2
                .apiInfo(apiInfo())            //用于定义api文档汇总信息
                .select().apis(RequestHandlerSelectors
                        .basePackage("com.alibaba.chaosblade.box.controller")) //指定生成文档的controller
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Chaosblade-box项目接口api") //文档标题
                .contact(new Contact("", //作者
                        "", "")) //联系人
                .description("Chaosblade-box项目api接口")//详细信息
                .version("1.0.0")//文档版本号
                .build();
    }
}
