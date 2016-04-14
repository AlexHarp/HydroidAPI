package au.gov.ga.hydroid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
@SpringBootApplication
public class HydroidApplication {

   private static Logger logger = LoggerFactory.getLogger(HydroidApplication.class);
   private static Properties applicationProperties = new Properties();

   private static String getConfigFilePath(String[] args) {
      // Default application.properties
      String configFilePath = "classpath:/application.properties";
      if (args == null) {
         return configFilePath;
      }
      for (String arg : args) {
         // Custom application.properties passed as command line argument
         if (arg.contains("spring.config.location")) {
            configFilePath = arg.substring(arg.indexOf("=") + 1);
            break;
         }
      }
      return configFilePath;
   }

   private static void setSystemProperties(String... keys) {
      for (String key : keys) {
         if (applicationProperties.getProperty(key, null) != null) {
            System.setProperty(key, applicationProperties.getProperty(key));
         }
      }
   }

   // Environment and property settings are only loaded after
   // the application.run method is called. This method will
   // load the configuration properties manually.
   private static void loadApplicationProperties(String[] args) {
      try {
         String configFilePath = getConfigFilePath(args);
         DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
         InputStream configInputStream = resourceLoader.getResource(configFilePath).getInputStream();
         applicationProperties.load(configInputStream);
         setSystemProperties("s3.use.file.system", "s3.use.file.system.path", "use.local.image.service");
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