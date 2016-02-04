package au.gov.ga.hydroid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by u24529 on 3/02/2016.
 */
@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class HydroidApplication {

   /*
   @Override
   protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
      return application.sources(HydroidApplication.class);
   }
   */

   public static void main(String[] args) {
      SpringApplication.run(HydroidApplication.class, args);
   }

}