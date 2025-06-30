package dev.mvc.team7;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.mvc.products.Products;
import dev.mvc.tool.Tool;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Windows: path = "C:/kd/deploy/team/products/storage";
        // ▶ file:///C:/kd/deploy/team/products/storage
      
        // Ubuntu: path = "/home/ubuntu/deploy/team/products/storage";
        // ▶ file:////home/ubuntu/deploy/team/products/storage
      
        // C:/kd/deploy/team/products/storage ->  /products/storage -> http://localhost:9091/products/storage;
//        registry.addResourceHandler("/products/storage/**").addResourceLocations("file:///" +  Products.getUploadDir());
        
//       // 제품 이미지용
      registry.addResourceHandler("/products/storage/**")
              .addResourceLocations("file:///C:/kd/deploy/resort/products/storage/");

        // 🔥 회원(member) 사업자 파일용
        registry.addResourceHandler("/member/storage/**")
                .addResourceLocations("file:///C:/kd/deploy/resort/member/storage/");
        
        // 이미지 URL 매핑
        registry.addResourceHandler("/calendar/storage/**")
                .addResourceLocations("file:///C:/kd/deploy/resort/calendar/storage/");

        // C:/kd/deploy/team/food/storage ->  /food/storage -> http://localhost:9091/food/storage;
        // registry.addResourceHandler("/food/storage/**").addResourceLocations("file:///" +  Food.getUploadDir());

        // C:/kd/deploy/team/trip/storage ->  /trip/storage -> http://localhost:9091/trip/storage;
        // registry.addResourceHandler("/trip/storage/**").addResourceLocations("file:///" +  Trip.getUploadDir());
        
    }
 
}
