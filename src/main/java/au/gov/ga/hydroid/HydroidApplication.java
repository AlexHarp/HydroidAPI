package au.gov.ga.hydroid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * Created by u24529 on 3/02/2016.
 */
@SpringBootApplication
public class HydroidApplication extends SpringBootServletInitializer {

   @Override
   protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
      return application.sources(HydroidApplication.class);
   }

   public static void main(String[] args) {
      SpringApplication hydroid = new SpringApplication(HydroidApplication.class);
      hydroid.addListeners(new ApplicationPidFileWriter("/usr/share/tomcat7/hydroid/hydroid.pid"));
      hydroid.run(args);
   }

}