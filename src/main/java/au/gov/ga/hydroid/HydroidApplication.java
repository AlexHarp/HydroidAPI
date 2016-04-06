package au.gov.ga.hydroid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
@SpringBootApplication
public class HydroidApplication {

   private static Logger logger = LoggerFactory.getLogger(HydroidApplication.class);
   private static Properties applicationProperties = new Properties();

   // Environment and property settings are only loaded after
   // the application.run method is called. This method will
   // load the configuration properties manually.
   private static void loadApplicationProperties(String[] args) {
      try {

         Map<String,String> envs = System.getenv();
         if (envs != null) {
            System.out.println("");
            System.out.println("Environment Vars");
            envs.keySet().forEach(key ->
               System.out.println(key + ": " + System.getenv(key))
            );
            System.out.println("");
         }

         Properties props = System.getProperties();
         if (props != null) {
            System.out.println("-----------------------");
            System.out.println("Properties.............");
            props.keySet().forEach(key ->
               System.out.println(key + ": " + System.getProperty(String.valueOf(key)))
            );
         }

         // Fallback to application.properties if the env var is not defined
         String configFilePath = "classpath:/application.properties";
         String configFilePathOverride = System.getenv("hydroid.spring.config.location");
         configFilePath = configFilePathOverride == null ? configFilePath : configFilePathOverride;
         DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
         InputStream configInputStream = resourceLoader.getResource(configFilePath).getInputStream();
         applicationProperties.load(configInputStream);
      } catch (Exception e) {
         logger.warn("loadApplicationProperties - Exception: ", e);
      }
   }

   private static String getProperty(String key) {
      return applicationProperties.getProperty(key);
   }

   public static void main(String[] args) {
      SpringApplication hydroid = new SpringApplication(HydroidApplication.class);
      hydroid.setBannerMode(Banner.Mode.OFF);
      loadApplicationProperties(args);
      String hydroidPidPath = getProperty("hydroid.pid.path");
      if (hydroidPidPath != null && !hydroidPidPath.isEmpty()) {
         hydroid.addListeners(new ApplicationPidFileWriter(hydroidPidPath));
      }
      hydroid.run(args);
   }

}