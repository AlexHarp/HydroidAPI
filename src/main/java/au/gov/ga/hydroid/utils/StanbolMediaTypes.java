package au.gov.ga.hydroid.utils;

import javax.ws.rs.core.MediaType;

/**
 * Created by u24529 on 3/02/2016.
 */
public class StanbolMediaTypes {

   private StanbolMediaTypes() {
   }

   public static final MediaType RDFN3 = new MediaType("text", "rdf+n3");
   public static final MediaType RDFXML = new MediaType("application", "rdf+xml");
   public static final MediaType TURTLE = new MediaType("text", "turtle");

}
