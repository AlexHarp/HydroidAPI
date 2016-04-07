package au.gov.ga.hydroid.service;

import javax.ws.rs.core.MediaType;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
public interface StanbolClient {

   public String enhance(String chainName, String content, MediaType outputFormat);
   public Properties findAllPredicates(String enhancedText);

}
