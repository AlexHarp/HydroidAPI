package au.gov.ga.hydroid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

/**
 * Created by u24529 on 3/02/2016.
 */
@SpringBootApplication
@Import({SchedulerConfiguration.class})
public class HydroidApplication extends SpringBootServletInitializer {

   @Override
   protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
      return application.sources(HydroidApplication.class);
   }

   public static void main(String[] args) {
      SpringApplication.run(HydroidApplication.class, args);
   }

}