package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.RestClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class RestClientImpl implements RestClient {

   private static final int TIMEOUT = 60;
   private ResteasyClientBuilder builder;

   @Autowired
   public RestClientImpl(HydroidConfiguration configuration) {
      builder = new ResteasyClientBuilder();
      builder.connectionPoolSize(5);
      builder.establishConnectionTimeout(TIMEOUT, TimeUnit.SECONDS);
      if (configuration.getProxyPort() > 0) {
         builder.defaultProxy(configuration.getProxyHost(), configuration.getProxyPort());
      }
   }

   public Response get(URI uri, MediaType acceptType) {
      WebTarget target = builder.build().target(uri);
      Builder httpRequest = target.request();
      if (acceptType != null) {
         httpRequest.accept(acceptType);
      }
      return httpRequest.get();
   }

   public Response post(URI uri, Entity<?> entity, MediaType acceptType) {
      WebTarget target = builder.build().target(uri);
      Builder httpRequest = target.request();
      if (acceptType != null) {
         httpRequest.accept(acceptType);
      }
      return httpRequest.post(entity);
   }

}
