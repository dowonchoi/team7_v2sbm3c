package dev.mvc.team7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"dev.mvc"})
@EnableJpaRepositories(basePackages = "dev.mvc.sms.repository")
@EntityScan(basePackages = "dev.mvc.sms.entity")
public class Team7V2sbm3cApplication {

	public static void main(String[] args) {
		SpringApplication.run(Team7V2sbm3cApplication.class, args);
	}

}
//package dev.mvc.team7;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.stereotype.Controller;
//
//@SpringBootApplication
//@ComponentScan(basePackages = {"dev.mvc"})
//public class Team7V2sbm3cApplication {
//
//  public static void main(String[] args) {
//    ApplicationContext context = SpringApplication.run(Team7V2sbm3cApplication.class, args);
//
//    System.out.println("---- 등록된 컨트롤러 목록 ----");
//    String[] controllers = context.getBeanNamesForAnnotation(Controller.class);
//    for (String name : controllers) {
//      System.out.println(">> " + name);
//    }
//  }
//}
