package dev.mvc.team7;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.mvc.products.Products;
import dev.mvc.member.MemberPath;
import dev.mvc.calendar.CalendarPath;
import dev.mvc.notice.NoticePath;
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
      // 아래 두 줄은 0804 오후 5시 20분 변경 전
//      registry.addResourceHandler("/products/storage/**")
//              .addResourceLocations("file:///home/ubuntu/team/products/storage/");
//      
        String productsUploadDir = Products.getUploadDir();
        registry.addResourceHandler("/products/storage/**")
                .addResourceLocations("file:///" + productsUploadDir);
        
        // 🔥 회원(member) 사업자 파일용
//        registry.addResourceHandler("/member/storage/**")
//                .addResourceLocations("file:///C:/kd/deploy/team/member/storage/");
        
        String memberUploadDir = MemberPath.getUploadDir(); // Member 클래스에 경로 메서드 추가
        registry.addResourceHandler("/member/storage/**")
                .addResourceLocations("file:///" + memberUploadDir);
        
        // 이미지 URL 매핑
//        registry.addResourceHandler("/calendar/storage/**")
//                .addResourceLocations("file:///C:/kd/deploy/team/calendar/storage/");
        
        String calendarUploadDir = CalendarPath.getUploadDir(); // Calendar 클래스에 경로 메서드 추가
        registry.addResourceHandler("/calendar/storage/**")
                .addResourceLocations("file:///" + calendarUploadDir);
        
//        registry.addResourceHandler("/uploads/notice/**")
//                .addResourceLocations("file:///C:/kd/deploy/team/notice/storage/");
        
        String noticeUploadDir = NoticePath.getUploadDir();
        registry.addResourceHandler("/uploads/notice/**")
                .addResourceLocations("file:///" + noticeUploadDir);
        
        // 리뷰용 이미지
        registry.addResourceHandler("/review/storage/**")
                .addResourceLocations("file:///C:/kd/deploy/team/review/storage/");
        
        // ✅ 외부 폴더(C:/kd/deploy/team/member_img/storage)를 /member_img/storage/** URL로 매핑
        registry.addResourceHandler("/member_img/storage/**")
                .addResourceLocations("file:///C:/kd/deploy/team/member_img/storage/");
        
        registry.addResourceHandler("/mms_img/**")
                .addResourceLocations("file:///C:/kd/deploy/mms/storage/");


        // C:/kd/deploy/team/food/storage ->  /food/storage -> http://localhost:9091/food/storage;
        // registry.addResourceHandler("/food/storage/**").addResourceLocations("file:///" +  Food.getUploadDir());

        // C:/kd/deploy/team/trip/storage ->  /trip/storage -> http://localhost:9091/trip/storage;
        // registry.addResourceHandler("/trip/storage/**").addResourceLocations("file:///" +  Trip.getUploadDir());
        
    }
 
}
