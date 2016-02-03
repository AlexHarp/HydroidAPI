package au.gov.ga.hydroid.service;

import org.openrdf.model.Statement;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by u24529 on 3/02/2016.
 */
public interface StanbolClient {

   public List<Statement> enhance(String chainName, String content, MediaType outputFormat) throws Exception;

}
