package au.gov.ga.hydroid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Application;

/**
 * Created by u24529 on 3/02/2016.
 */
@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class HydroidApplication extends SpringBootServletInitializer {

   @Override
   protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
      return application.sources(Application.class);
   }

   public static void main(String[] args) {
      SpringApplication.run(Application.class, args);
   }

}