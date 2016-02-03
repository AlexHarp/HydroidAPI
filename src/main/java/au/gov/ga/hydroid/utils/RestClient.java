package au.gov.ga.hydroid.utils;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Created by u24529 on 3/02/2016.
 */
public class RestClient {

   private static final int TIMEOUT = 60;

   private static ResteasyClientBuilder builder;

   static {
      builder = new ResteasyClientBuilder();
      builder.connectionPoolSize(5);
      builder.establishConnectionTimeout(TIMEOUT, TimeUnit.SECONDS);
      builder.defaultProxy("localhost", 3128);
      //builder.register(EnhancementStructureReader.class);
   }

   public static Response get(URI uri, MediaType acceptType) {
      WebTarget target = builder.build().target(uri);
      Builder httpRequest = target.request();
      if (acceptType != null) {
         httpRequest.accept(acceptType);
      }
      return httpRequest.get();
   }

   public static Response post(URI uri, Entity<?> entity, MediaType acceptType) {
      WebTarget target = builder.build().target(uri);
      Builder httpRequest = target.request();
      if (acceptType != null) {
         httpRequest.accept(acceptType);
      }
      return httpRequest.post(entity);
   }

}
