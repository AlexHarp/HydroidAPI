package au.gov.ga.hydroid.service;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by u24529 on 10/02/2016.
 */
public interface RestClient {

   public Response get(URI uri, MediaType acceptType);
   public Response post(URI uri, Entity<?> entity, MediaType acceptType);
   public Response postFile(URI uri, String fileName, InputStream fileInputStream) throws FileNotFoundException;
}
