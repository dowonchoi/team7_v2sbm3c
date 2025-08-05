package dev.mvc.team7;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.mvc.products.Products;
import dev.mvc.member.MemberPath;
import dev.mvc.calendar.CalendarPath;
import dev.mvc.notice.NoticePath;
import dev.mvc.tool.Tool;
import dev.mvc.mms_img.MMSImage;
import dev.mvc.openai.MemberImg;
import dev.mvc.review.Review;  // 추가


@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
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
        String reviewImgDir = Review.getUploadDir();
        registry.addResourceHandler("/review/storage/**")
                .addResourceLocations("file:///" + reviewImgDir);
        
        // 외부 폴더(C:/kd/deploy/team/member_img/storage)를 /member_img/storage/** URL로 매핑
        String memberImgDir = MemberImg.getUploadDir();
        registry.addResourceHandler("/member_img/storage/**")
                .addResourceLocations("file:///" + memberImgDir);
        
        // ✅ MMS 이미지 업로드 경로 설정
        String mmsUploadDir = MMSImage.getUploadDir(); // 정적 메서드 호출
        registry.addResourceHandler("/mms_img/**")
                .addResourceLocations("file:///" + mmsUploadDir);

    }
 
}
